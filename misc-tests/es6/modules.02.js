function reverseString(str) {
    let splitString = str.split("");
    let reverseArray = splitString.reverse();
    let joinArray = reverseArray.join("");
    return joinArray;
}

export function displayMessage(mess) {
	console.log(mess);
};

export function reverseMessage(mess) {
	console.log(reverseString(mess));
};
