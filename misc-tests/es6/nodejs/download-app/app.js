"use strict";

/**
 * To be consumed from the web page at index.html.
 * Could also be using an ES6 Promise.
 */

/* export */ function doDownload(urlToPull, fileToWrite, callback) {
    let url = '/download-service';
    // console.log(url);
    let xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === XMLHttpRequest.DONE) {
            // handle server response here if you want to
            let mess = `Back from server => ${xmlHttp.status}: ${xmlHttp.responseText}`;
            console.log(mess);
            if (callback !== undefined) {
                callback(mess);
            }
        }
    }

    xmlHttp.open("POST", url, true); // false for synchronous request

    xmlHttp.setRequestHeader("file-url", urlToPull); 
    xmlHttp.setRequestHeader("file-name", fileToWrite); 

    xmlHttp.send(null);  // null: no payload.
}

function uploadContent(type, content, fileToWrite, callback) {
    let url = '/upload-service';
    // console.log(url);
    let xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === XMLHttpRequest.DONE) {
            // handle server response here if you want to
            let mess = `Back from server => ${xmlHttp.status}: ${xmlHttp.responseText}`;
            console.log(mess);
            if (callback !== undefined) {
                callback(mess);
            }
        }
    }

    xmlHttp.open("POST", url, true); // false for synchronous request

    xmlHttp.setRequestHeader("Content-Type", type); 
    xmlHttp.setRequestHeader("file-name", fileToWrite); 

    xmlHttp.send(content);
}

// Expose
window.doDownload = doDownload; 
window.uploadContent = uploadContent;
