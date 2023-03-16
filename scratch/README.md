# Yo!
This is a test, for the maven repo hosted in ROB (<https://raw.githubusercontent.com/OlivierLD/ROB/repository>)

- `scratch.DewPoint` tests the availability of `common-utils` module.
- To test the `AstroComputer` module, do a 
```
$ ../gradlew clean build shadowJar
$ java -cp build/libs/scratch-1.0-all.jar celestial.almanac.JavaSample --now
```
