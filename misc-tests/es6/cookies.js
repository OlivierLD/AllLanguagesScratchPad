console.log("--- Cookies ---");
try {
    let cookieArray = document.cookie.split(';');
    cookieArray.forEach(cookie => {
        if (cookie.trim().indexOf('=') > -1) {
            let nameValuePair = cookie.split('=');
            console.log(`${nameValuePair[0].trim()} : [${nameValuePair[1].trim()}]`);
        }
    });
} catch (err) {
    console.err("Oops! ", err);
}
console.log("---------------");
