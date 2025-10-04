function checkPromiseState(promise) {
    return promise
        .then(() => "fulfilled")
        .catch(() => "rejected");
}

const myPromise = new Promise((resolve, reject) => {
    setTimeout(() => {
        resolve("Success!");
    }, 1000);
});

console.log("Before...");
checkPromiseState(myPromise)
    .then(state => {
        console.log(`Promise state: ${state}`);
    });
console.log("After!");