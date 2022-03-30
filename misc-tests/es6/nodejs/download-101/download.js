"use strict";


let http = require('http');
let fs = require('fs'); // Handle files

/**
 * Download a file from its URL.
 * 
 * @param {string} resourceURL URL of the file to get
 * @param {string} fileToWrite Name/Path of the file to write
 */
let downloadFile = (resourceURL, fileToWrite) => {
    // var fileToDownload = fileToGet; // req.body.fileToDownload;
    let file = fs.createWriteStream(fileToWrite); // "externalImage.jpg");
    let request = http.get(resourceURL, function(response) {
      response.pipe(file);
    });
};

let imgUrl = "http://localhost:8080/misc-tests/images/jconsole.2.png";
let localFileName = "image.png";

downloadFile(imgUrl, localFileName);


