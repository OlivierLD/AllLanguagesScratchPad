"use strict";

const fs = require("fs");
const express = require("express");

const app = express();
const PORT = 3000;

app.get("/", (req, res) => {
    res.render("index.ejs"); // in views folder
});

app.get("/samples", (req, res) => {
    res.render("samples/sample.ejs"); // in views folder
});

app.get("/files", (req, res) => {

    let path = req.query.path;
    let fileList = [];

    console.log(`Path is /files: ${path}`);
    let yes = true;

    if (!path || path === "") {
        console.log('No path (Blank or null)');
        res.render("files.ejs", {path : path, data : [], err : "Enter a valid directory in the field."});
        // console.log('Bam!');
        yes = false;
        // TODO return !
    } else {
        try {
            fileList = fs.readdirSync(path);
        } catch(err) {
            console.log("cannot find the directory");
            res.render("files.ejs", {path : path, data : [], err : "not a valid directory"});
            return;
        }
    }

    if (yes) {
        try {
            res.render("files.ejs", {path : path, data : fileList, err : ""}); // in views folder
        } catch(err) {
            console.log("ERROR in /files route");
        }
    }
});

app.get("/download", (req, res) => {

    let filePath = req.query.path;
    console.log(`Path is /download: ${filePath}`);
    res.download(filePath);

});

app.listen(PORT, () => {
    console.log(`listening on port ${PORT}`);
});