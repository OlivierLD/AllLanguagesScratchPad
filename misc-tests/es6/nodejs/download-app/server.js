/**
 * This is a small and tiny Web server. Mostly serves static pages and files.
 */
"use strict";

process.title = 'TinyNodeServer';

// HTTP port
let port = 8080;

let http = require('http');
let fs = require('fs');

let verbose = false;

let workDir = process.cwd();

console.log("----------------------------------------------------");
console.log("Usage: node " + __filename + " --verbose:true|false --port:XXXX --wdir:path/to/working/dir");
console.log("----------------------------------------------------");

for (let i=0; i<process.argv.length; i++) {
	console.log("arg #%d: %s", i, process.argv[i]);
}

if (typeof String.prototype.startsWith !== 'function') {
	String.prototype.startsWith = (str) => {
		return this.indexOf(str) === 0;
	};
}

if (typeof String.prototype.endsWith !== 'function') {
	String.prototype.endsWith = (suffix) => {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};
}

if (process.argv.length > 2) {
	for (let argc=2; argc<process.argv.length; argc++) {
		if (process.argv[argc].startsWith("--verbose:")) {
			let value = process.argv[argc].substring("--verbose:".length);
			if (value !== 'true' && value !== 'false') {
				console.log("Invalid verbose value [%s]. Only 'true' and 'false' are supported.", value);
				process.exit(1);
			}
			verbose = (value === 'true');
		} else if (process.argv[argc].startsWith("--port:")) {
			let value = process.argv[argc].substring("--port:".length);
			try {
				port = parseInt(value);
			} catch (err) {
				console.log("Invalid integer for port value %s.", value);
				process.exit(1);
			}
		} else if (process.argv[argc].startsWith("--wdir:")) {
			let value = process.argv[argc].substring("--wdir:".length);
			try {
				process.chdir(value);
				workDir = process.cwd();
			} catch (err) {
				console.log("Invalid new working directory %s.", value);
				process.exit(1);
			}
		} else {
			console.log("Unsupported parameter %s, ignored.", process.argv[argc]);
		}
	}
}

console.log("----------------------------------------------------");
console.log("Your working directory:", workDir);
console.log("----------------------------------------------------");
console.log("  Try http://localhost:8080/index.html");

/**
 * Download a file from its URL. Used by a REST service
 * 
 * @param {string} resourceURL URL of the file to get
 * @param {string} fileToWrite Name/Path of the file to write
 */
 let downloadFile = (resourceURL, fileToWrite) => {
    // var fileToDownload = fileToGet; // req.body.fileToDownload;
    let file = fs.createWriteStream(fileToWrite); // "externalImage.jpg");
    let request = http.get(resourceURL, function(response) {
      response.pipe(file)
	          .on('error', function(err) { if (err !== null) console.log(err) });
    });
};

let writeBase64DataToFile = (fileName, content) => { 

	// Convert from base64
	// Remove prefix like data:text/html;base64,
	const BASE64_REGEXP = new RegExp("^data:([a-z]*)/([a-z]*);base64,", "i");
	content = content.replace(BASE64_REGEXP, ""); // Remove base64 prefix

	let binBuf = Buffer.from(content, 'base64'); //.toString('ascii');
	// console.log("Converted content:");
	// console.log(binBuf);

	console.log(`Writing file: ${content.length} bytes long.`);
	// console.log(`binBuf is a ${typeof(binBuf)}`);
    let stream = fs.createWriteStream(fileName);
	stream.write(binBuf);
	stream.end();
};

/**
 * Small Simple and Stupid little web server.
 * Very basic. Lighter than Express.
 */
let handler = (req, res) => {
	let respContent = "";
	if (verbose) {
		console.log("Speaking HTTP from " + workDir); // was __dirname
		console.log("Server received an HTTP Request:\n" + req.method + "\n" + req.url + "\n-------------");
		console.log("ReqHeaders:" + JSON.stringify(req.headers, null, '\t'));
		console.log('Request:' + req.url);
		let prms = require('url').parse(req.url, true);
		console.log(prms);
		console.log("Search: [" + prms.search + "]");
		console.log("-------------------------------");
	}
	if (req.url.startsWith("/verbose=")) {
		if (req.method === "GET") {
			verbose = (req.url.substring("/verbose=".length) === 'on');
			res.end(JSON.stringify({verbose: verbose ? 'on' : 'off'}));
		}
	} else if (req.url.startsWith("/download-service")) { 
		if (req.method === "POST") {
			// Requires 2 parameters (Headers, "file-url", "file-name")
			let fileUrl = req.headers['file-url'];
			let fileName = req.headers['file-name'];

			console.log(`Will download ${fileName} from ${fileUrl}`);
			try {
				downloadFile(fileUrl, fileName);
				respContent = `Response from ${req.url}, all good (${fileName} created).`;
				res.writeHead(201, {'Content-Type': 'text/plain'});
				res.end(respContent);	
				console.log(`Download completed`);
			} catch (err) {
				respContent = "Response from " + req.url + "\n" + JSON.stringify(err);
				res.writeHead(201, {'Content-Type': 'text/plain'});
				res.end(respContent);	
				console.log(`Download errror: ${JSON.stringify(err)}`);
			}
		} else {
			respContent = `Unmanaged request. Response from ${req.method} ${req.url}`;
			res.writeHead(404, {'Content-Type': 'text/plain'});
			res.end(respContent);	
		}
	} else if (req.url.startsWith("/upload-service")) { 
		if (req.method === "POST") {
			// Requires 2 parameters (Headers, "file-url", "file-name")
			let fileType = req.headers['content-type'];
			let fileName = req.headers['file-name'];

			// console.log("Request:");
			// console.log(req);

			var body = '';

			req.on('data', (data) => {
				body += data;
				// Too much POST data, kill the connection!
				// 1e6 === 1 * Math.pow(10, 6) === 1 * 1000000 ~~~ 1MB
				if (body.length > 1e6) {
					req.connection.destroy();
				}
			});
	
			req.on('end', () => {
				console.log("...End of data.");
				// console.log(body);
				let content = body;
				console.log(`Content for ${fileName} is ${fileType}, ${content.length} bytes long.`);
				try {
					// Do something smart with the payload.
					writeBase64DataToFile(fileName, content);

					respContent = `Response from ${req.url}, all good (${fileName} created).`;
					res.writeHead(201, {'Content-Type': 'text/plain'});
					res.end(respContent);	
					console.log(`Upload completed`);
				} catch (err) {
					respContent = "Response from " + req.url + "\n" + JSON.stringify(err);
					res.writeHead(201, {'Content-Type': 'text/plain'});
					res.end(respContent);	
					console.log(`Upload errror: ${JSON.stringify(err)}`);
				}
			});

		} else {
			respContent = `Unmanaged request. Response from ${req.method} ${req.url}`;
			res.writeHead(404, {'Content-Type': 'text/plain'});
			res.end(respContent);	
		}
	} else if (req.url.startsWith("/")) { // Assuming static resource(s)
		if (req.method === "GET") {
			let resource = req.url.substring("/".length);
			if (resource.length === 0) {
				console.log('Defaulting to index.html');
				resource = 'index.html';
			}
			if (resource.indexOf("?") > -1) {
				resource = resource.substring(0, resource.indexOf("?"));
			}

			let exist = fs.existsSync(workDir + '/' + resource);

			if (exist === true && fs.lstatSync(workDir + '/' + resource).isDirectory()) {
				// try adding index.html
				console.log('Defaulting to index.html...');
				let resourceBackup = resource;
				resource += ((resource.endsWith("/") ? "" : "/") + "index.html");
				exist = fs.existsSync(workDir + '/' + resource);
				if (!exist) {
					resource = resourceBackup;
				} else {
					console.log(" >> From " + resourceBackup + " to " + resource);
				}
			} else {
				if (verbose) {
					console.log(workDir + '/' + resource + " not found");
				}
			}
			console.log((exist === true ? "Loading static " : ">> Warning: not found << ") + req.url + " (" + resource + ")");

			fs.readFile(workDir + '/' + resource,
					(err, data) => {
						if (err) {
							res.writeHead(400);
							return res.end('Error loading ' + resource);
						}
						if (verbose) {
							console.log("Read resource content:\n---------------\n" + data + "\n--------------");
						}
						let contentType = "text/plain"; // Default
						if (resource.endsWith(".css") || resource.endsWith(".css.map")) {
							contentType = "text/css";
						} else if (resource.endsWith(".html")) {
							contentType = "text/html";
						} else if (resource.endsWith(".xml")) {
							contentType = "text/xml";
						} else if (resource.endsWith(".js") || resource.endsWith(".js.map") || resource.endsWith(".map")) {
							contentType = "text/javascript";
						} else if (resource.endsWith(".jpg")) {
							contentType = "image/jpg";
						} else if (resource.endsWith(".jpeg")) {
							contentType = "image/jpeg";
						} else if (resource.endsWith(".gif")) {
							contentType = "image/gif";
						} else if (resource.endsWith(".png")) {
							contentType = "image/png";
						} else if (resource.endsWith(".ico")) {
							contentType = "image/x-icon";
						} else if (resource.endsWith(".svg")) {
							contentType = "image/svg+xml";
						} else if (resource.endsWith(".woff")) {
							contentType = "application/x-font-woff";
						} else if (resource.endsWith(".ttf")) {
							contentType = "application/octet-stream";
						} else {
							console.log("+-------------------------------------------")
							console.log("| Un-managed content type for " + resource);
							console.log("| You should add it in '%s'", __filename);
							console.log("+-------------------------------------------")
						}

						res.writeHead(200, {'Content-Type': contentType});
						//  console.log('Data is ' + typeof(data));
						if (resource.endsWith(".jpg") ||
								resource.endsWith(".jpeg") ||
								resource.endsWith(".gif") ||
								resource.endsWith(".ico") ||
								resource.endsWith(".svg") ||
								resource.endsWith(".woff") ||
								resource.endsWith(".ttf") ||
								resource.endsWith(".png")) {
							//  res.writeHead(200, {'Content-Type': 'image/gif' });
							res.end(data, 'binary');
						} else {
							res.end(data.toString().replace('$PORT$', port.toString())); // Replace $PORT$ with the actual port value.
						}
					});
		}
	} else {
		console.log("Unmanaged request: [" + req.url + "]");
		// respContent = "Response from " + req.url;
		res.writeHead(404, {'Content-Type': 'text/plain'});
		res.end(); // respContent);
	}
}; // HTTP Handler

/**
 * Helper function for escaping input strings
 */
function htmlEntities(str) {
	return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;')
			.replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/**
 * HTTP server
 */
console.log((new Date()) + ": Starting server on port " + port);
let server = http.createServer(handler);

process.on('uncaughtException', (err) => {
	if (err.errno === 'EADDRINUSE') {
		console.log("Address in use.");
	} else {
		console.log(err);
	}
	process.exit(1);
});

try {
	server.listen(port, () => {
		console.log((new Date()) + ": Server is listening on port " + port);
	});
} catch (err) {
	console.log("There was an error:");
	console.log(err);
}
