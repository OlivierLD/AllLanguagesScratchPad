function reverseString(str) {
    let splitString = str.split("");
    let reverseArray = splitString.reverse();
    let joinArray = reverseArray.join("");
    return joinArray;
}

function reverseMessage(mess) {
	console.log(reverseString(mess));
};

export default reverseMessage;
