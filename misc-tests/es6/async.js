"use strict";

let x = 1000;
async function firstFunction(){
    for(let i=0; i<x; i++){
      // do something
    }
    console.log("Done with first function");
    return;
  };
  
  async function secondFunction(){
    await firstFunction();
    // now wait for firstFunction to finish...
    // do something else
    console.log("Done");
  };

  secondFunction();
