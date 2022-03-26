## Compare the class version (ES6) vs no-class version (ES6 too)

Non-class version
```text
$ time node oci2gecko.js -i ../input.json -o converted.json --max-l 1 --max-c 80
----------------------------------------------------
Running from /Users/olediour/repos/AllLanguagesScratchPad/JSON2SRT/ES6
Usage: node oci2gecko.js --help
----------------------------------------------------
[W]: addPostPunct: begin = 545136, beginTime = 544896, post_p= &
[W]: addPostPunct: begin = 812928, beginTime = 812736, post_p= %
[W]: addPostPunct: begin = 2053392, beginTime = 2053152, post_p= +
[W]: addPostPunct: begin = 2053776, beginTime = 2053632, post_p= =
[W]: addPostPunct: begin = 2455072, beginTime = 2454864, post_p= &
[OK] same word sequence
Conversion Completed

real	0m0.160s
user	0m0.127s
sys	0m0.040s
```

Class version
```text
$ time node oci2gecko_v2.js -i ../input.json -o converted_v2.json --max-l 1 --max-c 80
----------------------------------------------------
Running from /Users/olediour/repos/AllLanguagesScratchPad/JSON2SRT/ES6
Usage: node oci2gecko_v2.js --help
----------------------------------------------------
[W]: addPostToken: end = 545375, _endTime= 545135, post_p= &
[W]: addPostToken: end = 813360, _endTime= 812928, post_p= %
[W]: addPostToken: end = 2053632, _endTime= 2053392, post_p= +
[W]: addPostToken: end = 2054160, _endTime= 2053776, post_p= =
[W]: addPostToken: end = 2455279, _endTime= 2455071, post_p= &
Conversion Completed

real	0m0.185s
user	0m0.135s
sys	0m0.050s
```

Same result:
```text
$ diff converted_v2.json converted.json 
$ 
```
No difference

---
