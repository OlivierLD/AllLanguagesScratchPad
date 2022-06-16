"use strict";

const x = 1000;
async function firstFunction(){
  for (let i=0; i<x; i++) {
    // do something here...
    // setTimeout(() => console.log(`i is now ${i}`), 500);
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
