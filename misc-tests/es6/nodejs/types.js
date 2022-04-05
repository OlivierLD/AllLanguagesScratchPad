"use strict";

const THAT_ONE = 10;
const THIS_ONE = "Akeu Coucou!";
const THIRD_ONE = true;

const BUFFER32 = new ArrayBuffer(8);
let view32   = new Int32Array(BUFFER32);

const BUFFER16 = new ArrayBuffer(8);
let view16   = new Int16Array(BUFFER16);

const BUFFER8 = new ArrayBuffer(8);
let view8   = new Int8Array(BUFFER8);

for (var i=0; i<view32.length; i++) {
    let rnd = Math.random();
    // console.log(`Rnd: ${rnd}`);
    view32[i] = (rnd * 1000).toFixed(0);
}
for (var i=0; i<view16.length; i++) {
    let rnd = Math.random();
    // console.log(`Rnd: ${rnd}`);
    view16[i] = (rnd * 1000).toFixed(0);
}
for (var i=0; i<view8.length; i++) {
    let rnd = Math.random();
    // console.log(`Rnd: ${rnd}`);
    view8[i] = (rnd * 1000).toFixed(0);
}

console.log(`Type of that one: ${typeof(THAT_ONE)}`);
console.log(`Type of this one: ${typeof(THIS_ONE)}`);
console.log(`Type of third one: ${typeof(THIRD_ONE)}`);

console.log(`Type of BUFFER: ${typeof(BUFFER32)}`);
console.log(`Type of view: ${typeof(view32)}`);

console.log("--------------------------");
// for (let i=0; i<view.length; i++) {
//     console.log(`INT32ARRAY ELEMENT: ${view[i]} (${typeof(view[i])}) `);
// }
view32.forEach(v => console.log(`INT32ARRAY ELEMENT: ${v} (${typeof(v)}) `));
console.log("--------------------------");
view16.forEach(v => console.log(`INT16ARRAY ELEMENT: ${v} (${typeof(v)}) `));
console.log("--------------------------");
view8.forEach(v => console.log(`INT8ARRAY ELEMENT: ${v} (${typeof(v)}) `));
console.log("--------------------------");

// ------------

const typeSizes = {
"undefined": () => 0,
"boolean": () => 4,
"number": () => 8,
"string": item => 2 * item.length,
"object": item => !item ? 0 : Object
    .keys(item)
    .reduce((total, key) => sizeOf(key) + sizeOf(item[key]) + total, 0)
};

const sizeOf = value => typeSizes[typeof(value)](value);

console.log(`Sizes:`);

console.log(`Size of that one: ${sizeOf(THAT_ONE)} bytes`);
console.log(`Size of this one: ${sizeOf(THIS_ONE)} bytes`);
console.log(`Size of third one: ${sizeOf(THIRD_ONE)} bytes`);

console.log("--------------------------");
view32.forEach(v => console.log(`INT32ARRAY ELEMENT: ${v} (size: ${sizeOf(v)} bytes) `));
console.log("--------------------------");
view8.forEach(v => console.log(`INT8ARRAY ELEMENT: ${v} (size: ${sizeOf(v)} bytes) `));
console.log("--------------------------");
