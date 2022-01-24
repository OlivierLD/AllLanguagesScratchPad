// Can run in nodeJS
let one = {
  one: "ONE"
};

let two = {
  two: "TWO"
};

let ar = [];
ar.push(one);
ar.push(two);

console.log("--- Let's go ---");
// Populated, OK.
console.log(`Raw: ${ar}`);
console.log(`Stringified: ${JSON.stringify(ar)}`);

console.log("--- Reversing ---");
let ar3 = [];
let ar4 = [];
ar.forEach(el => {
  ar3.push(el); // THAT is a deep clone
  ar4.push(el); // THAT is a deep clone
});

let ar2 = ar; // This does NOT clone the object
// let ar2 = ar.reverse();  // Warning!!! This reverses ar, and put in in ar2. ar IS reversed.
ar2.reverse();
console.log(`Reversed Raw: ${ar2}`);
console.log(`Reversed Stringified: ${JSON.stringify(ar2)}`);
console.log(`!! Wierd Reversed Stringified: ${JSON.stringify(ar.reverse())}`);

console.log(`Clone Reversed Stringified: ${JSON.stringify(ar3.reverse())}`);

// Basic objects
console.log("--- Basics (one) ---");
console.log(`Raw JSON ${one}`);
console.log(`Stringified JSON ${JSON.stringify(one)}`);
console.log(`Parsed JSON ${JSON.parse('{ "akeu": "coucou" }')}`);
console.log(`Stringified Parsed JSON ${JSON.stringify(JSON.parse('{ "akeu": "coucou" }'))}`);

console.log("--- Streaming AR3 ---");
ar3.forEach(item => {
  console.log(`-- Raw:${item} => Stringed:${JSON.stringify(item)}`);
});
console.log("--- Streaming AR4 ---");
ar4.forEach(item => {
  console.log(`-- Raw:${item} => Stringed:${JSON.stringify(item)}`);
});
ar4.reverse();
console.log("--- Streaming AR4, reversed ---");
ar4.forEach(item => {
  console.log(`-- Raw:${item} => Stringed:${JSON.stringify(item)}`);
});

console.log("--- With Joins (don't do that!!) ---");
let joinedArray = ar2.join('');
console.log(`Joined Raw - 1: ${joinedArray}`);
console.log(`Joined Raw - 2: ${ar2.join('')}`);
console.log(`Joined Stringified: ${JSON.stringify(joinedArray)}`);

console.log("--- Done ---");

