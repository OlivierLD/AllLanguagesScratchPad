"use strict";

const BUFFER32 = new ArrayBuffer(8);
let view32   = new Int32Array(BUFFER32);

const BUFFER16 = new ArrayBuffer(8);
let view16   = new Int16Array(BUFFER16);

const BUFFER8 = new ArrayBuffer(8);
let view8   = new Int8Array(BUFFER8);

console.log(`View32 length: ${view32.length}`);
for (let i=0; i<view32.length; i++) {
    let rnd = Math.random();
    view32[i] = (rnd * 1000).toFixed(0);
    console.log(`Rnd: ${rnd} -> ${view32[i]}`);
}

console.log(`Typeof view32[0]: ${typeof(view32[0])}`);
console.log(` view32[0] is integer: ${Number.isInteger(view32[0])}`);
console.log(` view32[0]: ${view32[0]}`);

console.log(` Number maxValue: ${Number.MAX_VALUE}`);
console.log(` view32[0] maxValue: ${view32[0].MAX_VALUE}`);

console.log("========================");

console.log(`View8 length: ${view8.length}`);
for (let i=0; i<view8.length; i++) {
    let rnd = Math.random();
    // console.log(`Rnd: ${rnd}`);
    view8[i] = (rnd * 1000).toFixed(0);
}
console.log(`Typeof view8[0]: ${typeof(view8[0])}`);
console.log(` view8[0] is integer: ${Number.isInteger(view8[0])}`);

console.log(` Number maxValue: ${Number.MAX_VALUE}`);
console.log(` view8[0] maxValue: ${view8[0].MAX_VALUE}`);

view8[0] = 1.7976931348623157e+308;
console.log(`Typeof view8[0]: ${typeof(view8[0])}`);
console.log(` view8[0] is integer: ${Number.isInteger(view8[0])}`);
console.log(` view8[0] : ${view8[0]}`);

view8[0] = 1234;
console.log(`Typeof view8[0]: ${typeof(view8[0])}`);
console.log(` view8[0] is integer: ${Number.isInteger(view8[0])}`);
console.log(` view8[0] : ${view8[0]}`);

view8[0] = 128;
console.log(`Typeof view8[0]: ${typeof(view8[0])}`);
console.log(` view8[0] is integer: ${Number.isInteger(view8[0])}`);
console.log(` view8[0] : ${view8[0]}`);

console.log(`Int8Array,  bytes per element: ${Int8Array.BYTES_PER_ELEMENT}`);
console.log(`Int16Array, bytes per element: ${Int16Array.BYTES_PER_ELEMENT}`);
console.log(`Int32Array, bytes per element: ${Int32Array.BYTES_PER_ELEMENT}`);
//console.log(`Int64Array, bytes per element: ${Int64Array.BYTES_PER_ELEMENT}`);
