# A (NodeJS) Web App to download a file
This is using a Node JS server script (`server.js`) to handle static pages, and REST requests.

There is a REST endpoint that will allow a file to be uploaded on the server.  
`POST /download-service -H "file-url: http://url-to-resource" -H "file-name: duh.jpg"`

## Start the server
```
$ node server.js
```

## Identify a URL pointing to a file to upload
Like `http://localhost:8080/palm.04.jpg`, or better, `http://donpedro.lediouris.net/journal/trip/P1241389.JPG` in our case.

## Web Client
From a browser, reach <http://localhost:8080/index.html>, and use the page to download the file (from its URL).

## `curl` Client
```
$ curl -X POST http://localhost:8080/upload-service -H "file-url: http://donpedro.lediouris.net/journal/trip/P1241389.JPG" -H "file-name: duh.jpg"
```

After execution, you should see a `duh.jpg` on the server.

# A (NodeJS) Web App to upload a file
Start `node server.js` from the `download-app` directory, and reach <http://localhost:8080/upload.html> from your browser.  

The client part is done in `upload.html`, function `process`, and in `app.js`, function `uploadContent`.  
The server part in implemented in `server.js`, by the service `POST /upload-service`.  
Important: See the management of the data going back and forth (file content), they are base64 encoded, you need to be aware all this.

### Use case
The IP of your server-machine is like `100.111.136.104`, you have started the node server with
```
$ node server.js --port:1234
```
From a browser pointing on `http://100.111.136.104:1234/upload.html`, you upload a file like `image.jpg`, that will be uploaded on the server, as `new_image.jpg`.  
Once the process is completed, you might be able to reach the new image, from <http://100.111.136.104:1234/new_image.jpg>.

> Make sure the file types you deal with are managed in `server.js`, around lines `230` or so....

# On NodeJS, from a web age, ask the server to get to a resource, with its _OS path_ (_not_ a URL)
Start the server (`npm run start`), and from anywhere on the same network, reach <http://the.machine:XXXX/from.server.html>.

Then, in this page, give the path _**on the system where the server runs**_, like `/home/pi/repos/raspberry-coffee/RESTNavServer/launchers/web/icons/sailboat.jpg`, and
click the button. You should see the image (if this is an image) on the page.

---
