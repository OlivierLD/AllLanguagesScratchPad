// Good resource at https://www.datchley.name/es6-promises/

// this function returns a Promise
let getSum = (n1, n2) => {

	let isAnyNegative = () => {
		return n1 < 0 || n2 < 0;
	};

	let promise = new Promise((resolve, reject) => {
		if (isAnyNegative()) {
			reject(Error("Negative numbers not supported"));
		}
		resolve(n1 + n2);
	});
	return promise;
};

let testIt = (a, b) => {
	getSum(a, b)
			.then(
					// this is the resolve function
					(result) => {
						console.log("---- 1 - S U C C E S S ----");
						console.log("Success: Result is ", result);
						console.log("-----------------------");
					},
					// this is the reject function
					(error) => {
						console.log("--- 1 - R E J E C T E D ---");
						console.log("Oops:", error);
						console.log("-----------------------");
					});
}

let testIt_2 = (a, b) => {
	getSum(a, b)
			.then(
					// this is the resolve function
					(result) => {
						console.log("---- 2 - S U C C E S S ----");
						console.log("Success: Result is ", result);
						console.log("-----------------------");
					})
			.catch (
					// this is the cath function
					(error) => {
						console.log("--- 2 - C A T C H ---");
						console.log("Oops:", error);
						console.log("-----------------------");
					});
}

console.log("--- RESOLVE / REJECT ---");
testIt(5, 6);
testIt(5, -6);

console.log("--- THEN.CATCH ---");
testIt_2(5, 6);
testIt_2(5, -6);
