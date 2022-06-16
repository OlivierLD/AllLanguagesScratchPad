"use strict";

function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

async function demo(ms, callback) {
	console.log('Taking a break...');
	await sleep(ms);
    if (callback) {
        callback(ms);
    }
	console.log('End of async');
}

console.log("-- Start --");
demo(2000, (val) => {
    console.log(`This is the callback with value ${val}`);
});
console.log("--- End ---");
