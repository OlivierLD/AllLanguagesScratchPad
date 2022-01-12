## Consume the Speech Service
This shows how to _consume_ the Speech Service.

### Build the Speech Service
First, on a new OCI Compute Instance, build the Speech Service as explained [here](https://bitbucket.oci.oraclecorp.com/projects/ODA/repos/bots-speech/browse).

### Then consume it

> Note: Watch the `gradle.properties`, enable the proxy(ies).

- Java SDK, from the Bug Bash page: <https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm>, see `com.oracle.bmc.aispeech.SampleAIServiceSpeechClient`. 
- In Java, REST approach, see `oliv.rest.client.SampleSpeechRESTClient`.
- In Java, WebSocket approach, see `oliv.ws.client.SampleSpeechWSClient`.
- In Python, REST approach, see `rest_client.py`.
- In Python, WebSocket approach, see `ws_speech_client.py`.

### Usage
#### Java
To build the samples from the directory where `build.gradle` lives:
```
$ ../gradlew clean shadowJar
```
Then, to run the examples:  
REST approach:
```
$ ./run.rest.sample.sh
```
WebSocket approach:
```
$ ./run.ws.sample.sh 
```

Look into the shell scripts for details and options.

#### Python (use Python3)
May require a 
```
$ pip3 install [--proxy http://www-proxy.us.oracle.com:80] requests
$ pip3 install [--proxy http://www-proxy.us.oracle.com:80] json
$ pip3 install [--proxy http://www-proxy.us.oracle.com:80] websocket-client
```
REST request, from the directory where `foo.wav` lives:
```
$ python3 src/main/python/rest_client.py --audio-file:foo.wav
Response: "Change the name to waking up"
Bye!
```

WebSocket request, from the directory where `foo.wav` lives:
```
$ python3 src/main/python/ws_speech_client.py 
Sending foo.wav for processing
### Connection Opened ###
Service Response: "Change the name to waking up"
Closing WS Connection...
Thread terminating.
### Connection Closed ###
Bye
```

### Notes
The `tyrus` (Java WebSocket RI) version has problems...

---
