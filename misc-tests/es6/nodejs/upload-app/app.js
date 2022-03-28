"use strict";

/**
 * To be consumed from the web page at index.html.
 * Could also be using an ES6 Promise.
 */

/* export */ function doUpload(urlToPull, fileToWrite) {
    let url = '/upload-service';
    // console.log(url);
    let xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === XMLHttpRequest.DONE) {
            //handle server response here if you want to
            console.log(`Back from server => ${xmlHttp.status}: ${xmlHttp.responseText}`);
        }
    }

    xmlHttp.open("POST", url, true); // false for synchronous request

    xmlHttp.setRequestHeader("file-url", urlToPull); 
    xmlHttp.setRequestHeader("file-name", fileToWrite); 

    xmlHttp.send(null);  // null: no payload.
}

window.doUpload = doUpload; // Expose
