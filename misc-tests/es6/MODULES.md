# Quick Notes about Modules

## From a Web App
See `modules.02.js`:
```javascript
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
```
Two functions are exported, a third one is consumed internally.  
See `modules.02.consume.js`:
```javascript
import * as mod from  './modules.02.js';

mod.displayMessage("Hello JavaScript World!");
mod.reverseMessage("Hello JavaScript World!");
```
Notice that you _cannot_ invoke this file from NodeJS, the `import` statement is not recognized.
But it can be used from a Web App (`html`):
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Consume Modules</title>
    <script type="module" src="./modules.02.consume.js"></script>
</head>
<body>
    Check the Console.
</body>
</html>
```
Notice the `type="module"` in the `<script>` element.   
The expected output will be seen in the JS console of the browser. 
> _Note_: To avoid CORS errors, you need to start a web server to access this page, like 
> ```
> node rest.server.js
> ```
> and then access <http://localhost:8080/modules.html> from your browser.

---

### `export default () =>...`
See a `export default` of an _**anonymous function**_ sample in
`module.01.bis.js` and `module.01.ter.js`, and the way to consume them
from `modules.bis.html`.

## From NodeJS
See `modules.03.js`:
```javascript
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
```

Things to notice:  
- the `exports` statement (and _not_ `export`), to expose functions externally

Consume it:  
See `module.03.consume.js`:
```javascript
const mod = require('./modules.03');

mod.displayMessage("Hello JavaScript World!");
mod.reverseMessage("Hello JavaScript World!");
```
It `require`s the `modules.03` (notice: there is no `,js` extension), and invokes the exposed (exported)
functions.

```
$ node modules.03.consume.js 
Hello JavaScript World!
!dlroW tpircSavaJ olleH
```

Note that you cannot invoke the `reverseString` method, as it has not been exported.