## JSON to SRT

SRT (Speech Recognition Tool ?) goes with [Gecko](https://github.com/gong-io/gecko).

### Java flavor
Build it first
```text
$ ../gradlew shadowJar
```
Then run it
```text
$ CP=./build/libs/JSON2SRT-1.0-all.jar
$ java -cp ${CP} oci.convert.OCIJson2Srt_v2 -i input.json -o output.srt --max-l 1 --max-c 80
```


### ES6 flavor
```text
$ node json2srt.js -i ../input.json -o converted.srt --max-l 1 --max-c 80
```

#### Debugging NodeJS
See <https://nodejs.org/en/docs/guides/debugging-getting-started/>

Use `--inspect`
```text
$ node --inspect json2srt.js -i ../input.json -o output.srt  --max-l 1 --max-c 80
```
