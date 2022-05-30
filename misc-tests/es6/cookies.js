
let cookieArray = document.cookie.split(';');
cookieArray.forEach(cookie => {
    let nameValuePair = cookie.split('=');
    console.log(`${nameValuePair[0].trim()} : [${nameValuePair[1].trim()}]`);
});
