const myPromise = new Promise((resolve, reject) => {
    setTimeout(() => {
        resolve("Successfull");
    }, 1000);
});

async function checkPromiseState(prm) {
    try {
        const result = await myPromise;
        console.log("Promise is pending or fulfilled. (" + prm + ")");

        // Handle the fulfilled state here

    } catch (error) {
        console.log("Promise is rejected.");
    }
}

console.log("Calling First One...");
checkPromiseState(1);
console.log("Calling Second One...");
checkPromiseState(2);
console.log("There we are.");