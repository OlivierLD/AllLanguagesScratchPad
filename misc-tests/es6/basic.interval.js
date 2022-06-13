"use strict";

let value = 0;

let interval = setInterval(() => {
    if (value < 10) {
        value += 1;
        console.log(`Value is now ${value}.`);
    } else {
        console.log(`Exiting.`);
        clearInterval(interval);
    }
}, 1000);

setTimeout(() => {
    console.info("Done");
}, 12000);

console.log("Starting !");

