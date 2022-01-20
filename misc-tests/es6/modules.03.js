function reverseString(str) {
    let splitString = str.split("");
    let reverseArray = splitString.reverse();
    let joinArray = reverseArray.join("");
    return joinArray;
}

function displayMessage(mess) {
	console.log(mess);
};

function reverseMessage(mess) {
	console.log(reverseString(mess));
};

exports.displayMessage = displayMessage;
exports.reverseMessage = reverseMessage;
