# Download a file with NodeJS
From one console (any directory), start the NodeJS server (for static documents)
```
$ node <path/to/script>/server.js
```
Make sure you have a url, pointing to a file that exists, like 
`http://localhost:8080/misc-tests/images/jconsole.2.png`, or `http://donpedro.lediouris.net/journal/trip/P1241389.JPG`...

Then, from another console:
```
$ node download.js
```

This scripts `download.js` will get the image from the URL above, and copy it into `image.png`.

