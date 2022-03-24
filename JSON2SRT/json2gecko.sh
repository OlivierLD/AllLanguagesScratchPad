#!/bin/bash
CP=./build/libs/JSON2SRT-1.0-all.jar
if [[ -f ${CP} ]]; then
  echo -e "${CP} was found"
else
  echo -e "${CP} was NOT found"
fi
java -cp ${CP} oci.convert.OCIJson2OScribe -i input.json -o output.json --max-l 1 --max-c 80
