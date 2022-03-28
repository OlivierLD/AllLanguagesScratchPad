# A (NodeJS) Web App to upload a file
This is using a Node JS server script (`server.js`) to handle static pages, and REST requests.

There is a REST endpoint that will allow a file to be uploaded on the server.  
`POST /upload-service -H "file-url: http://url-to-resource" -H "file-name: duh.jpg"`

## Start the server
```
$ node server.js
```

## Identify a URL pointing to a file to upload
Like `http://localhost:8080/palm.04.jpg` in our case.

## Web Client
From a browser, reach <http://localhost:8080/index.html>, and use the page to upload the file (from its URL).


## CURL Client
```
$ curl -X POST http://localhost:8080/upload-service -H "file-url: http://localhost:8080/palm.04.jpg" -H "file-name: duh.jpg"
```

After execution, you should see a `duh.jpg` on the server.

---
