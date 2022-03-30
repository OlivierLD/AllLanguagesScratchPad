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
. . .


---
