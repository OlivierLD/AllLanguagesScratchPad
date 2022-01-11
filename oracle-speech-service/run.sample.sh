#!/bin/bash
CP=./build/libs/SpeechJavaSDK_01-1.0-all.jar
# LOGGING_FLAGS="-Djava.util.logging.config.file=./logging.properties"
# LOGGING_FLAGS="-Dlog4j.debug=true -Dlog4j.configuration=file:///log4j.properties"
LOGGING_FLAGS="-Dlog4j.debug=true" # Will use the log4j.properties located in the resources folder
#
java -cp ${CP} ${LOGGING_FLAGS} com.oracle.bmc.aispeech.SampleAIServiceSpeechClient
