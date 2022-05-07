
"use strict";

const VERBOSE = false;

/**
 * Translates an hexa color code into rgba or hexa
 * 
 * @param {string} hex The Hex string to translate. Starts with '#', then 3 or 4 two-digit hex numbers, like #ff0088 od #ff008880
 * @param {number} opacity in [0..1], opacity to apply, default 1.0.
 * @param {boolean} toHex Translate to 'rgba(...)' (default) of new hexa number (if set to true).
 * @returns {string} the expected encoded color
 */
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
    let regExp3 = new RegExp('^#([A-Fa-f0-9]{2}){3}$');
    let regExp4 = new RegExp('^#([A-Fa-f0-9]{2}){4}$');

    if (regExp3.test(hex)) { // 3 numbers, RGB
        if (VERBOSE) {
            console.log("-> Matching 3");
        }
        hexArray = hex.substring(1).split(''); // Drop first '#'
    } else if (regExp4.test(hex)) {  // 4 numbers, RGBA
        if (VERBOSE) {
            console.log("-> Matching 4");
        }
        hexArray = hex.substring(1).split(''); // Drop first '#'
    }
    if (hexArray) {
        // console.log("Found:" + hexArray);
        let newHex = '0x' + hexArray.join('');
        if (VERBOSE) {
            console.log("Generated: " + newHex);
        }
        let newRGBAStr = '';
        if (hexArray.length === 6) {
            if (toHex) {
                newRGBAStr = `#${
                    lpad(((newHex >> 16) & 255).toString(16), 2, '0')
                }${
                    lpad(((newHex >> 8) & 255).toString(16), 2, '0')
                }${
                    lpad((newHex & 255).toString(16), 2, '0')
                }${
                    lpad(parseInt((255 * opacity).toFixed(0)).toString(16), 2, '0')}`;
            } else {
                newRGBAStr = `rgba(${(newHex >> 16) & 255}, ${(newHex >> 8) & 255}, ${newHex & 255}, ${opacity})`;
            }
        } else if (hexArray.length === 8) {
            // Will override opacity
            if (toHex) {
                newRGBAStr = `#${ 
                    lpad(((newHex >> 24) & 255).toString(16), 2, '0')
                }${
                    lpad(((newHex >> 16) & 255).toString(16), 2, '0')
                }${
                    lpad(((newHex >> 8) & 255).toString(16), 2, '0')
                }${
                    lpad(parseInt((255 * opacity).toFixed(0)).toString(16), 2, '0')}`;
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
console.log(`${hexValue}, transparency 0.6 => RGBA: ${rgba}`);

hexValue = '#fbafff01';
rgba = hexToRgba(hexValue);
console.log(`${hexValue}, default transparency => RGBA: ${rgba}`);

hexValue = '#FF00FF';
rgba = hexToRgba(hexValue);
console.log(`${hexValue}, uppercase, default transparency => RGBA: ${rgba}`);

hexValue = '#fbafff01';
rgba = hexToRgba(hexValue, 0.6);
console.log(`${hexValue}, overriding transparency => RGBA: ${rgba}`);

hexValue = '#fbafff';
rgba = hexToRgba(hexValue, 0.05, true);
console.log(`${hexValue}, transparency 0.05, to rgba => RGBA: ${rgba}`);

hexValue = '#fbafff01';
rgba = hexToRgba(hexValue, 0.1, true);
console.log(`${hexValue}, transparency 0.1, to rgba => RGBA: ${rgba}`);

try {
    hexValue = '#abc';
    rgba = hexToRgba(hexValue, 0.05, true);
    console.log(`${hexValue} => RGBA: ${rgba}`);
} catch (err) {
    console.error(`Cought ${err}`);
}

try {
    hexValue = '#abca';
    rgba = hexToRgba(hexValue, 0.05, true);
    console.log(`${hexValue} => RGBA: ${rgba}`);
} catch (err) {
    console.error(`Cought ${err}`);
}


