#!/bin/bash
CP=./build/libs/oracle-speech-service-1.0-all.jar
LOGGING_FLAGS="-Djava.util.logging.config.file=./logging.properties"
# LOGGING_FLAGS="-Dlog4j.debug=true -Dlog4j.configuration=file:///log4j.properties"
# LOGGING_FLAGS="-Dlog4j.debug=true" # Will use the log4j.properties located in the resources folder
#
JAVA_OPTS=
JAVA_OPTS="${JAVA_OPTS} -Dverbose=true"
JAVA_OPTS="${JAVA_OPTS} -Dspeak-up=true"
# JAVA_OPTS="${JAVA_OPTS} -Dslow-down=true"
#
java -cp ${CP} ${LOGGING_FLAGS} ${JAVA_OPTS} oliv.ws.client.SampleSpeechWSClient
