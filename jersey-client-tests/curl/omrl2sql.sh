#!/bin/bash
#
# run from bots/runtime/libs/bots-inbuilt-components
# Requires a ../../../gradlew shadowJar
#
# Usage examples:
#  ./src/test/scripts/omrl2sql.sh --help
#  ./src/test/scripts/omrl2sql.sh --mapping-schema:./src/main/resources/omrl.mapping.schema.generated.03.json --schema-name:journal_committee --query-index:10 --execute-query:false
#
# ./src/test/scripts/omrl2sql.sh --mapping-schema:./src/test/resources/datasets/omrl/utils/OMRL_base_schema.json \
#                                --mapping-sql-schema:./src/test/resources/datasets/omrl/utils/OMRL_sql_schema.json \
#                                --query-file:./src/test/resources/datasets/omrl/utils/omrl.race_track.query.02.json \
#                                --struct-output:false
# ./src/test/scripts/omrl2sql.sh --mapping-schema:./src/test/resources/datasets/omrl/utils/OMRL_base_schema.json --mapping-sql-schema:./src/test/resources/datasets/omrl/utils/OMRL_sql_schema.json --query-file:./src/test/resources/datasets/omrl/utils/omrl.race_track.query.02.json --struct-output:false
#
# To format the output:
# Use ./src/test/scripts/omrl2sql.sh [prms] | jq
#
CP=./build/libs/inbuilt-components-19.1.3-100-all.jar
CP=${CP}:./build/classes/java/main:./build/classes/java/test
java -cp ${CP} oracle.cloud.bots.component.omrl.utils.StandaloneOMRL2SQL "$@"
