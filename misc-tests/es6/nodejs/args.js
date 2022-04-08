"use strict";

let args = [7, 3, 8];
let [h] = args.reverse(); // args is now 8, 3, 7
console.log(`args: ${args}`);
console.log(`h: ${h}`);

args = [7, 3, 8]; // Reset to original
[h] = [...args].reverse(); // args is still 7, 3, 8
console.log(`args: ${args}`);
console.log(`h: ${h}`);
