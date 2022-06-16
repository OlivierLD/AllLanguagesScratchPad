"use strict";

setTimeout(function () {
	console.log("One");
	setTimeout(function () {
		console.log("Two");
		setTimeout(function () {
			console.log("Three");
		}, 1000);
	}, 1000);
}, 1000);

// setTimeout(() => {
// 	console.log("Un");
// 	setTimeout(() => {
// 		console.log("Deux");
// 		setTimeout(() => {
// 			console.log("Trois");
// 		}, 1000);
// 	}, 1000);
// }, 1000);
