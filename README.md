# AllLanguagesScratchPad
Bits and pieces, in several languages

### Some bulk notes
To upgrade Gradle
```
$ ./gradlew wrapper --gradle-version=7.0
```
Before upgrading, do a 
```
$ ./gradlew --warning-mode all
```
Dependencies
```
$ ../gradlew dependencies --configuration <compileClasspath | runtimeClasspath>
```
