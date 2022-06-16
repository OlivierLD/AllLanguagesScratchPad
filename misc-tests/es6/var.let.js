"use strict";

// See https://www.valentinog.com/blog/var/?utm_source=newsletter&utm_medium=email&utm_campaign=var_let_and_const_a_cheatsheet&utm_term=2022-04-23

var aVar = 10; // Global 

console.log(`The var: ${aVar}`);

function test() {

	aVar = 20;
	console.log(`The var: ${aVar}`);

	let num = 100; // In its block

	console.log("Value: " + num);
	{
		console.log("Inner Block");
		let num = 200;
		console.log("Value: " + num);
		
		aVar = 30;
		console.log(`The var: ${aVar}`);
	}
	console.log("Outside > Value: " + num);
	console.log(`The var: ${aVar}`);
};

test();

console.log(`The var: ${aVar}`);
