"use strict";

function test() {

	let num = 100;

	console.log("Value: " + num);
	{
		console.log("Inner Block");
		let num = 200;
		console.log("Value: " + num);
	}
	console.log("Outside > Value: " + num);
};
test();
