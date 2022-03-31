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

const USE_READ_AS_DATA_URL = 1;
const USE_READ_AS_ARRAY_BUFFER = 2;
const USE_READ_AS_TEXT = 3;

let readOption = USE_READ_AS_ARRAY_BUFFER;
let readServerOption = USE_READ_AS_DATA_URL;

function directDownload(resourceURL, fileToWrite, callback) {
    let request = new XMLHttpRequest();
    request.open('GET', resourceURL, true);
    request.responseType = 'blob'; // ??
    request.onload = () => {
        let reader = new FileReader();
        reader.onload = (e) => {
            console.log("Data:" + e.target.result);
            if (readOption === USE_READ_AS_ARRAY_BUFFER) {
                let file = new File([e.target.result], fileToWrite); // Works with readAsArrayBuffer
                console.log("File created");
            }
        };
        if (readOption === USE_READ_AS_DATA_URL) {
            reader.readAsDataURL(request.response); // Returns Base64 encoded
        } else if (readOption === USE_READ_AS_ARRAY_BUFFER) {
            reader.readAsArrayBuffer(request.response);
        } else if (readOption === USE_READ_AS_TEXT) {
            reader.readAsText(request.response); 
        }
    };
    request.onerror = (ev) => {
        if (callback !== undefined) {
            callback("Error (also see the console):" + JSON.stringify(ev.target, null, 2));
        } else {
            console.error("Error (also see the console):" + JSON.stringify(ev.target, null, 2));
        }
    }
    request.send();
};

function loadFileFromServer(pathOnTheServer, fileToWrite, callback) {
    let request = new XMLHttpRequest();
    request.open('GET', `/read-local-file-service?file-name=${pathOnTheServer}`, true);
    if (readServerOption === USE_READ_AS_ARRAY_BUFFER || readServerOption === USE_READ_AS_DATA_URL) {
        request.responseType = 'blob'; // ??
    }
    request.onload = () => {
        let reader = new FileReader();
        reader.onload = (e) => {
            console.log("Data:\n" + e.target.result);
            if (readServerOption === USE_READ_AS_ARRAY_BUFFER) {
                let file = new File([e.target.result], fileToWrite); // Works with readAsArrayBuffer
                console.log("File created");
                if (callback !== undefined) {
                    callback(e.target.result);
                }
            } else if (readServerOption === USE_READ_AS_DATA_URL) {
                if (callback !== undefined) {
                    callback(e.target.result);
                }
            }
        };
        if (readServerOption === USE_READ_AS_DATA_URL) {
            reader.readAsDataURL(request.response); // Returns Base64 encoded
        } else if (readServerOption === USE_READ_AS_ARRAY_BUFFER) {
            reader.readAsArrayBuffer(request.response);
        } else if (readServerOption === USE_READ_AS_TEXT) {
            reader.readAsText(request.response); 
        }
    };
    request.onerror = (ev) => {
        if (callback !== undefined) {
            callback("Error (also see the console):" + JSON.stringify(ev.target, null, 2));
        } else {
            console.error("Error (also see the console):" + JSON.stringify(ev.target, null, 2));
        }
    }
    request.send();
};
  
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
window.directDownload = directDownload;
window.loadFileFromServer = loadFileFromServer;
