"use strict";

const x = 1000;

async function print(x) {
  console.log(`  - Value is now ${x}`);
  return;
};

async function firstFunction(){
  for (let i=0; i<x; i++) {
    // do something here...
    // setTimeout(() => console.log(`i is now ${i}`), 500);
    await print(i);
  }
  console.log("Done with first function");
  return;
};

async function secondFunction(){
  await firstFunction();
  // now wait for firstFunction to finish...
  // do something else here
  console.log("Done with second function");
};

secondFunction();
console.log(">> End of script <<");
