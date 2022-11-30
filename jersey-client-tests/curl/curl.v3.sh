#!/bin/bash
#
# Adding SQL Select entities
#
#################################################################################
# Creating Diego's properties for Backend Mappings, on race_track (race, track) #
#################################################################################
#
# jq cheat-sheet: https://lzone.de/cheat-sheet/jq
# Some of my jq samples: https://confluence.oraclecorp.com/confluence/display/AARCH/Playground#Playground-EditaDecisionService
#
# Diego's REST EndPoints: https://confluence.oraclecorp.com/confluence/display/IBS/Design+Time+Implementation+for+C2SQL+MVP#DesignTimeImplementationforC2SQLMVP-ForeignKey
#
# Oliv
ODA_INSTANCE=100.111.136.104
# Savio
# ODA_INSTANCE=100.111.136.92
#
BOT_ID=9753307D-EEA8-4851-BDC7-922EEA52C373
# 0B046E6F-4AB6-4421-84B3-AAE6259A218D
# 4BC8AE4E-63FB-4A15-8188-FBC999B75DDF
BOTS_TENANT_ID=odaserviceinstance00
#
echo -en "BOT_ID is ${BOT_ID}, is that all right ? > "
read -r RESP
if [[ ! ${RESP} =~ ^(yes|y|Y)$ ]]; then
  echo -en "Enter new BOT_ID > "
  read -r BOT_ID
  echo -e "Moving on with ${BOT_ID} [Hit Return]"
  read -r RESP
fi
#
# REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/29660F52-305A-449E-AF67-C7D91C3140E2
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities
#
# Create COMPOSITEENTITY sql_race
#
PAYLOAD="{
  \"name\": \"sql_race\",
  \"type\": \"COMPOSITEENTITY\",
  \"description\": \"Entity with sqlSelect\"
}"
curl --location --request POST "${REST_REQ}" \
                       --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                       --header 'Accept: application/json' \
                       --header 'Content-Type: application/json' \
                       --data-raw "${PAYLOAD}" > out.txt
#
SQL_ENTITY_ID=$(jq '.id' out.txt)
SQL_ENTITY_ID=$(echo "${SQL_ENTITY_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "SQL_RACE is ${SQL_ENTITY_ID}"
echo -e "------------------------------"
echo -e "Hit [return]"
read -r RESP
#
# List attributes for one entity
# http://100.111.136.104:9990/management-api/v1/bots/A624FF72-D030-4F26-890E-9C80827CB129/entities/B220B523-BB8D-4AD6-9554-6C6D38F9C8F0/attributes
#
#########################################
#
# sql_race Entity.
#
# Need race_name, race_class, race_date, race_concat
# No PK
# No FK
#
echo -en "Create EntityBackendMapping for 'sql_race' ? [Hit Return] "
read -r RESP
#
# Create EntityBackendMapping for entity 'sql_race'
#
# Note: use sqlSelect, not sqlExpression below.
PAYLOAD_1="{
  \"name\": \"sql_race\",
  \"type\": \"SQL\",
  \"dataServiceName\": \"race_track\",
  \"tableName\": null,
  \"sqlSelect\": \"SELECT NAME as RACE_NAME, RACE_CLASS, RACE_DATE, (RACE_CLASS || ' - ' || NAME || ' on ' || RACE_DATE) as RACE_CONCAT FROM RACE\",
  \"defaultOrderBy\": null,
  \"primaryKey\": null
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/backendMappings
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_1}" | jq > out.txt
#
cat out.txt | jq
#
echo -en "[Hit Return] "
read -r RESP
#
BACKEND_ENTITY_SQL=$(jq '(.id)' out.txt)
BACKEND_ENTITY_SQL=$(echo "${BACKEND_ENTITY_SQL}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created backendMapping for sql_race ${BACKEND_ENTITY_SQL}"
echo -e "------------------------------"
#
# Create sql_race attributes
# race_name, race_class, race_date, race_concat
#
# 1 - race_name
PAYLOAD="{
  \"name\": \"race_name\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 1
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# 2 - race_class
PAYLOAD="{
  \"name\": \"race_class\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 2
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# 3 - race_date
PAYLOAD="{
  \"name\": \"race_date\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 3
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# 4 - race_concat
PAYLOAD="{
  \"name\": \"race_concat\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 4
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
echo -en "[Hit Return] "
read -r RESP
#
# Display created attributes
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' > out.txt
#
# jq . out.txt
echo -e "---------------------------"
echo -e "Created Attributes in sql_race:"
jq '.items[] | [.name, .type ] | join(" type ")' out.txt
echo -en "[Hit Return] "
read -r RESP
#
echo -en "Moving on (to Create AttributeBackendMapping for column race_name in sql_race) ? [Hit Return] "
read -r RESP
########### sql_race.race_name
# Find the id of "race_name" in the "sql_race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
                      --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                      --header 'Accept: application/json' > out.txt
SQL_RACE_NAME_ID=$(jq '.items[] | select (.name == "race_name") | "\(.id)" ' out.txt)
SQL_RACE_NAME_ID=$(echo "${SQL_RACE_NAME_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "SQL_RACE.RACE_NAME is ${SQL_RACE_NAME_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column race_name
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes/${SQL_RACE_NAME_ID}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_SQL}\", \"type\": \"SQL\"
  },
  \"columnName\": \"RACE_NAME\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column RACE_NAME"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_NAME_ID=$(jq '(.id)' out.txt)
BACKEND_RACE_NAME_ID=$(echo "${BACKEND_RACE_NAME_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for SQL_RACE.RACE_NAME: ${BACKEND_RACE_NAME_ID}"
echo -e "------------------------------"
echo -en "Moving on (to Create AttributeBackendMapping for column race_class in sql_race) ? [Hit Return] "
read -r RESP
########### sql_race.race_class
# Find the id of "race_class" in the "sql_race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
                      --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                      --header 'Accept: application/json' > out.txt
SQL_RACE_CLASS_ID=$(jq '.items[] | select (.name == "race_class") | "\(.id)" ' out.txt)
SQL_RACE_CLASS_ID=$(echo "${SQL_RACE_CLASS_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "SQL_RACE.RACE_CLASS is ${SQL_RACE_CLASS_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column race_class
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes/${SQL_RACE_CLASS_ID}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_SQL}\", \"type\": \"SQL\"
  },
  \"columnName\": \"RACE_CLASS\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column RACE_CLASS"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_NAME_ID=$(jq '(.id)' out.txt)
BACKEND_RACE_NAME_ID=$(echo "${BACKEND_RACE_NAME_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for SQL_RACE.RACE_NAME: ${BACKEND_RACE_NAME_ID}"
echo -e "------------------------------"
echo -en "Moving on (to Create AttributeBackendMapping for column race_date in sql_race) ? [Hit Return] "
read -r RESP
########### sql_race.race_date
# Find the id of "race_date" in the "sql_race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
                      --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                      --header 'Accept: application/json' > out.txt
SQL_RACE_DATE_ID=$(jq '.items[] | select (.name == "race_date") | "\(.id)" ' out.txt)
SQL_RACE_DATE_ID=$(echo "${SQL_RACE_DATE_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "SQL_RACE.RACE_DATE is ${SQL_RACE_DATE_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column race_date
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes/${SQL_RACE_DATE_ID}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_SQL}\", \"type\": \"SQL\"
  },
  \"columnName\": \"RACE_DATE\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column RACE_DATE"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_DATE_ID=$(jq '(.id)' out.txt)
BACKEND_RACE_DATE_ID=$(echo "${BACKEND_RACE_DATE_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for SQL_RACE.RACE_DATE: ${BACKEND_RACE_DATE_ID}"
echo -e "------------------------------"
#
# PAUSED HERE
#
echo -en "Moving on (to Create AttributeBackendMapping for column race_concat in sql_race) ? [Hit Return] "
read -r RESP
########### sql_race.race_concat
# Find the id of "race_concat" in the "sql_race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
                      --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                      --header 'Accept: application/json' > out.txt
SQL_RACE_CONCAT_ID=$(jq '.items[] | select (.name == "race_concat") | "\(.id)" ' out.txt)
SQL_RACE_CONCAT_ID=$(echo "${SQL_RACE_CONCAT_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "SQL_RACE.RACE_CONCAT is ${SQL_RACE_CONCAT_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column race_concat
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${SQL_ENTITY_ID}/attributes/${SQL_RACE_CONCAT_ID}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_SQL}\", \"type\": \"SQL\"
  },
  \"columnName\": \"RACE_CONCAT\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column RACE_CONCAT"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_CONCAT_ID=$(jq '(.id)' out.txt)
BACKEND_RACE_CONCAT_ID=$(echo "${BACKEND_RACE_CONCAT_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for SQL_RACE.RACE_CONCAT: ${BACKEND_RACE_CONCAT_ID}"
echo -e "------------------------------"
#
echo -e "Done!"

