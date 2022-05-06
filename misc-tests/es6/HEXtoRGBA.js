
"use strict";

const VERBOSE = false;

let hexToRgba = (hex, opacity=1.0, toHex=false) => {

    let lpad = (str, len, pad) => {
        let s = str;
        while (s.length < len) {
            s = (pad === undefined ? ' ' : pad) + s;
        }
        return s;
    };
    
    if (VERBOSE) {
        console.log(` Processing ${hex}`);
    }
    let hexArray;
    let regExp3 = new RegExp('^#([A-Fa-f0-9]{3}){1,2}$');
    let regExp4 = new RegExp('^#([A-Fa-f0-9]{4}){1,2}$');

    // if (/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) { // 3 numbers, RGB
    if (regExp3.test(hex)) { // 3 numbers, RGB
        hexArray = hex.substring(1).split('');
    // } else if (/^#([A-Fa-f0-9]{4}){1,2}$/.test(hex)) {  // 4 numbers, RGBA
    } else if (regExp4.test(hex)) {  // 4 numbers, RGBA
        hexArray = hex.substring(1).split('');
    }
    if (hexArray) {
        if (hexArray.length === 3) {
            if (VERBOSE) {
                console.log("Reshaping, len=3");
            }
            hexArray = [
                hexArray[0], hexArray[0], 
                hexArray[1], hexArray[1], 
                hexArray[2], hexArray[2]
            ];
        }
        // console.log("Found:" + hexArray);
        let newHex = '0x' + hexArray.join('');
        if (VERBOSE) {
            console.log("Generated: " + newHex);
        }
        let newRGBAStr = '';
        if (hexArray.length === 6) {
            if (toHex) {
                newRGBAStr = `#${ lpad(((newHex >> 16) & 255).toString(16), 2, '0')}${lpad(((newHex >> 8) & 255).toString(16), 2, '0')}${lpad((newHex & 255).toString(16), 2, '0')}${lpad(parseInt((255 * opacity).toFixed(0)).toString(16), 2, '0')}`;
            } else {
                newRGBAStr = `rgba(${(newHex >> 16) & 255}, ${(newHex >> 8) & 255}, ${newHex & 255}, ${opacity})`;
            }
        } else if (hexArray.length === 8) {
            // Will override opacity
            if (toHex) {
                newRGBAStr = `#${ lpad(((newHex >> 24) & 255).toString(16), 2, '0')}${lpad(((newHex >> 16) & 255).toString(16), 2, '0')}${lpad(((newHex >> 8) & 255).toString(16), 2, '0')}${lpad(parseInt((255 * opacity).toFixed(0)).toString(16), 2, '0')}`;
            } else {
                newRGBAStr = `rgba(${(newHex >> 24) & 255}, ${(newHex >> 16) & 255}, ${(newHex >> 8) & 255}, ${opacity})`;
            }
        }
        return newRGBAStr;
    } else {
        throw new Error(`Bad Hex ${hex}`);
    }
}

// Tests
let hexValue = '#fbafff';
let rgba = hexToRgba(hexValue, 0.6);
console.log(`${hexValue} => RGBA: ${rgba}`);

hexValue = '#fbafff01';
rgba = hexToRgba(hexValue, 0.6);
console.log(`${hexValue} => RGBA: ${rgba}`);

hexValue = '#fbafff';
rgba = hexToRgba(hexValue, 0.05, true);
console.log(`${hexValue} => RGBA: ${rgba}`);

hexValue = '#fbafff01';
rgba = hexToRgba(hexValue, 0.1, true);
console.log(`${hexValue} => RGBA: ${rgba}`);

