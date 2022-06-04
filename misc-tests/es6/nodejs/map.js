// Map sample
// The 'map' function applies to arrays.

"use strict"

const originalJSON = {
    "end": 6.9,
    "start": 0,
    "speaker": {
        "id": "N/A"
    },
    "words": [
        {
            "start": 0,
            "end": 0.25,
            "text": "it's",
            "confidence": 1
        },
        {
            "start": 0.25,
            "end": 0.31,
            "text": "A",
            "confidence": 1
        },
        {
            "start": 0.31,
            "end": 0.69,
            "text": "really",
            "confidence": 1
        },
        {
            "start": 0.69,
            "end": 1.19,
            "text": "exciting",
            "confidence": 1
        },
        {
            "start": 1.19,
            "end": 1.5,
            "text": "Stage",
            "confidence": 1
        },
        {
            "start": 1.5,
            "end": 1.62,
            "text": "in",
            "confidence": 1
        },
        {
            "start": 1.62,
            "end": 1.8,
            "text": "the",
            "confidence": 1
        },
        {
            "start": 1.8,
            "end": 2.34,
            "text": "lifecycle",
            "confidence": 1
        },
        {
            "start": 2.34,
            "end": 2.46,
            "text": "of",
            "confidence": 1
        },
        {
            "start": 2.46,
            "end": 2.52,
            "text": "a",
            "confidence": 1
        },
        {
            "start": 2.52,
            "end": 3.11,
            "text": "technology",
            "confidence": 1
        },
        {
            "start": 3.11,
            "end": 3.52,
            "text": "company",
            "confidence": 1
        }
    ]
};

let txOne = (obj) => {
    return {
        start: obj.start,
        end: obj.end,
        speaker: obj.speaker,
        terms: obj.words
    };
};

let txTwo = (obj) => {
    return {
        start: obj.start,
        end: obj.end,
        text: obj.text,
        confidence: obj.confidence,
        type: "WORD"
    };
};

let txThree = (obj) => {
    return {
        start: obj.start,
        end: obj.end,
        speaker: obj.speaker,
        terms: obj.words.map(o => txTwo(o))
    };
};

let obj1 = txOne(originalJSON);
console.log("1 - TX Result:\n", obj1);
console.log("----------------------------------------");

let newArr = originalJSON.words.map(o => txTwo(o));
// Also supported syntax:
let newArr2 = originalJSON.words.map(txTwo);
console.log("2 - TX Result:\n", newArr);
console.log("----------------------------------------");

let obj2 = txThree(originalJSON);
console.log("3 - TX Result:\n", obj2);
console.log("----------------------------------------");

console.log("4 - All implicit function/TX Result:\n", originalJSON.words.map(obj => {
    return {
        start: obj.start / 2.0,
        end: obj.end * 1.23,
        text: obj.text.toUpperCase(),
        confidence: obj.confidence / Math.random()
    };
}));
console.log("----------------------------------------");

