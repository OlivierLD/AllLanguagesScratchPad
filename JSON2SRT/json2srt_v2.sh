#!/bin/bash
CP=./build/libs/JSON2SRT-1.0-all.jar
if [[ -f ${CP} ]]; then
  echo -e "${CP} was found"
else
  echo -e "${CP} was NOT found"
fi
# java -cp json-20180813.jar:commons-cli-1.4.jar OCIJson2Srt.java -i input.json -o output.srt --max-l 1 --max-c 80
# java -cp ${CP} -verbose oci.convert.OCIJson2Srt -i input.json -o output.srt --max-l 1 --max-c 80
java -cp ${CP} oci.convert.OCIJson2Srt_v2 -i input.json -o output.srt --max-l 1 --max-c 80
