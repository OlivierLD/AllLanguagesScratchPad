"use strict";


let obj1 = { name: 'Tintin' };
let obj2 = { name: 'Haddock' };
let obj3 = { name: 'Milou' };

let arr = [ obj1, obj2, obj3 ];

let newArr = [];

let concat = (array, unTruc) => {
    if (Array.isArray(unTruc)) {
        unTruc.forEach(truc => array.push(truc));
    } else {
        array.push(unTruc);
    }
};

console.log(`Obj1 is an array: ${Array.isArray(obj1)}`);
console.log(`arr is an array: ${Array.isArray(arr)}`);

concat(newArr, obj1);
console.log(`NewArr: ${JSON.stringify(newArr, null, 2)}`);

concat(newArr, obj2);
console.log(`NewArr: ${JSON.stringify(newArr, null, 2)}`);

concat(newArr, arr);
console.log(`NewArr: ${JSON.stringify(newArr, null, 2)}`);

