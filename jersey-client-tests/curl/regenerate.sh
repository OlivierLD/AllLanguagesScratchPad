#!/bin/bash
#
# Requires a ../../../gradlew shadowJar
#
# Example:
#  ./regenerate.sh --spider-output:../../../tools/sqlparser/OMRL_v3_out.json > good.txt 2> bad.txt
#
CP=./build/libs/inbuilt-components-19.1.3-100-all.jar
java -cp ${CP} oracle.cloud.bots.component.omrl.utils.StandaloneOMRL2SQLTester $*
