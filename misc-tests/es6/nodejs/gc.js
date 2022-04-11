"use strict";

try {
    if (global.gc) {
        console.log("Cleaning!");
        global.gc();
    } else {
        console.log("gc not available");
        console.log("Try node --expose-gc gc.js");
    }
} catch (e) {
    console.log("node --expose-gc gc.js");
    process.exit(); // Bam!
}
