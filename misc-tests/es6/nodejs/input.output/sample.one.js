"use strict";
//
// Read user's input (stdin).
// From https://nodejs.org/learn/command-line/accept-input-from-the-command-line-in-nodejs
//

const readline = require('node:readline');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

console.log("Let's go!...");

rl.question(`What's your name? `, name => {
  console.log(`Hi ${name}!`);
  rl.close();
});