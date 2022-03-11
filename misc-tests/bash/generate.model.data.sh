#!/bin/bash
#
# Write some java code, from a text file.
# Use ./generate.model.data.sh > temp.java
#
LINE_NO=1
ONE_LINE=
#
FILE_TO_READ=model.data.txt
# for line in $(cat model.data.txt); do
while IFS= read -r line; do
  # echo -e "line: ${line}"
  if [[ ${LINE_NO} == 1 ]]; then
    ONE_LINE="matrix.put(\"${line}\", new String[] { \"- Released -\", "
  else
    ONE_LINE="${ONE_LINE}\"${line}\""
    if [[ ${LINE_NO} -gt 1 ]] && [[ ${LINE_NO} -lt 5 ]]; then
      ONE_LINE="${ONE_LINE}, "
    fi
  fi
  #
  if [[ ${LINE_NO} == 5 ]]; then
    ONE_LINE="${ONE_LINE} });"
    echo ${ONE_LINE}
    LINE_NO=0
    ONE_LINE=
  fi
  LINE_NO=$((LINE_NO+1))
done < "${FILE_TO_READ}"

