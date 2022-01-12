## Consume the Speech Service

> Note: Watch the `gradle.properties`, enable the proxy(ies).

- Java SDK, from the Bug Bash page: <https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm>, see `com.oracle.bmc.aispeech.SampleAIServiceSpeechClient`. 
- In Java, REST approach, see `oliv.rest.client.SampleSpeechRESTClient`.
- In Java, WebSocket approach, see `oliv.ws.client.SampleSpeechWSClient`.

### Usage
#### Java
To build the samples from the directory where `build.gradle` lives:
```
$ ../gradlew clean shadowJar
```
Then, to run the examples:  
REST approach:
```
$ ./run.rst.sample.sh
```
WebSocket approach:
```
$ ./run.ws.sample.sh 
```

Look into the shell scripts for details and options.

#### Python
May require a 
```
$ pip3 install [--proxy http://www-proxy.us.oracle.com:80] requests
$ pip3 install [--proxy http://www-proxy.us.oracle.com:80] json
```
REST request, from the directory where `foo.wav` lives:
```
$ python3 src/main/python/rest_client.py --audio-file:foo.wav
Response: "Change the name to waking up"
Bye!
```

### Notes
The `tyrus` (Java WebSocket RI) version has problems...

---
