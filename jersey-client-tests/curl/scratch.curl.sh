#!/bin/bash
#
#  THIS IS A SCRATCH-PAD !!
#
##########################################################################################
# Creating/Querying Diego's properties for Backend Mappings, on race_track (race, track) #
##########################################################################################
#
# jq cheat-sheet: https://lzone.de/cheat-sheet/jq
# Some of my jq samples: https://confluence.oraclecorp.com/confluence/display/AARCH/Playground#Playground-EditaDecisionService
#
# Diego's REST EndPoints: https://confluence.oraclecorp.com/confluence/display/IBS/Design+Time+Implementation+for+C2SQL+MVP#DesignTimeImplementationforC2SQLMVP-ForeignKey
#
ODA_INSTANCE=100.111.136.104
#
BOT_ID=82DA6F6B-7F41-461B-A1A1-4A35D6DEFD6A
BOTS_TENANT_ID=odaserviceinstance00
#
echo -en "BOT_ID is ${BOT_ID}, is that all right ? > "
read -r RESP
if [[ ! ${RESP} =~ ^(yes|y|Y)$ ]]
then
  echo -en "Enter new BOT_ID > "
  read -r BOT_ID
  echo -e "Moving on with ${BOT_ID} [Hit Return]"
  read -r RESP
fi
#
# REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/29660F52-305A-449E-AF67-C7D91C3140E2
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/
#
# echo -e "Executing ${REST_REQ}"
#
# 1 - Find Entity ID for 'track'
# curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "track") | {id} '
TRACK_ENTITY_ID=$(curl --location --request GET "${REST_REQ}" \
                       --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                       --header 'Accept: application/json' | jq '.items[] | select (.name == "track") | "\(.id)" ')
TRACK_ENTITY_ID=$(echo "${TRACK_ENTITY_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK is ${TRACK_ENTITY_ID}"
echo -e "------------------------------"
# 2 - Find Entity ID for 'race'
RACE_ENTITY_ID=$(curl --location --request GET "${REST_REQ}" \
                      --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                      --header 'Accept: application/json' | jq '.items[] | select (.name == "race") | "\(.id)" ')
RACE_ENTITY_ID=$(echo "${RACE_ENTITY_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE is ${RACE_ENTITY_ID}"
echo -e "------------------------------"
#
# List bag items for one entity
# http://100.111.136.104:9990/management-api/v1/bots/A624FF72-D030-4F26-890E-9C80827CB129/entities/B220B523-BB8D-4AD6-9554-6C6D38F9C8F0/bagItems
#
#########################################
#
# TRACK Entity.
#
echo -en "List BackendEntityMapping for 'track' ? Hit Return > "
read -r RESP
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/backendMappings
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request GET "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Content-Type: application/json' | jq
#
echo -en "Hit Return > "
read -r RESP
# cat out.txt
BACKEND_ENTITY_TRACK=$(jq '(.id)' out.txt)
BACKEND_ENTITY_TRACK=$(echo "${BACKEND_ENTITY_TRACK}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created backendMapping for track ${BACKEND_ENTITY_TRACK}"
echo -e "------------------------------"

echo -en "Moving on (to Create AttributeBackendMapping for column track_id in track) ? Hit Return > "
read -r RESP
########### track.track_id
# Find the id of "track_id" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems
TRACK_TRACK_ID=$(curl --location --request GET "${REST_REQ}" \
                      --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                      --header 'Accept: application/json' | jq '.items[] | select (.name == "track_id") | "\(.id)" ')
TRACK_TRACK_ID=$(echo "${TRACK_TRACK_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.TRACK_ID is ${TRACK_TRACK_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column track_id
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems/${TRACK_TRACK_ID}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_TRACK}\", \"type\": \"SQL\"
  },
  \"columnName\": \"Track_ID\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Track_ID"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_TRACK_ID=$(jq '(.id)' out.txt)
BACKEND_TRACK_ID=$(echo "${BACKEND_TRACK_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for track.Track_ID: ${BACKEND_TRACK_ID}"
echo -e "------------------------------"
########### track.name
echo -en "Moving on (to Create AttributeBackendMapping for column name in track) ? Hit Return > "
read -r RESP
#
# Find the id of "name" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems
TRACK_NAME=$(curl --location --request GET "${REST_REQ}" \
                  --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                  --header 'Accept: application/json' | jq '.items[] | select (.name == "name") | "\(.id)" ')
TRACK_NAME=$(echo "${TRACK_NAME}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.NAME is ${TRACK_NAME}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column name
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems/${TRACK_NAME}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_TRACK}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Name\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Track.Name"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_TRACK_NAME=$(jq '(.id)' out.txt)
BACKEND_TRACK_NAME=$(echo "${BACKEND_TRACK_NAME}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for track.name: ${BACKEND_TRACK_NAME}"
echo -e "------------------------------"
########### track.location
echo -en "Moving on (to Create AttributeBackendMapping for column location in track) ? Hit Return > "
read -r RESP
#
# Find the id of "location" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems
TRACK_LOCATION=$(curl --location --request GET "${REST_REQ}" \
                      --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                      --header 'Accept: application/json' | jq '.items[] | select (.name == "location") | "\(.id)" ')
TRACK_LOCATION=$(echo "${TRACK_LOCATION}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.LOCATION is ${TRACK_LOCATION}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column location
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems/${TRACK_LOCATION}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_TRACK}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Location\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Track.Location"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_TRACK_LOCATION=$(jq '(.id)' out.txt)
BACKEND_TRACK_LOCATION=$(echo "${BACKEND_TRACK_LOCATION}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for track.location: ${BACKEND_TRACK_LOCATION}"
echo -e "------------------------------"
########### track.seating
echo -en "Moving on (to Create AttributeBackendMapping for column seating in track) ? Hit Return > "
read -r RESP
#
# Find the id of "location" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems
TRACK_SEATING=$(curl --location --request GET "${REST_REQ}" \
                     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                     --header 'Accept: application/json' | jq '.items[] | select (.name == "seating") | "\(.id)" ')
TRACK_SEATING=$(echo "${TRACK_SEATING}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.SEATING is ${TRACK_SEATING}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column seating
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems/${TRACK_SEATING}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_TRACK}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Seating\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Track.Seating"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_TRACK_SEATING=$(jq '(.id)' out.txt)
BACKEND_TRACK_SEATING=$(echo "${BACKEND_TRACK_SEATING}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for track.seating: ${BACKEND_TRACK_SEATING}"
echo -e "------------------------------"
########### track.year_opened
echo -en "Moving on (to Create AttributeBackendMapping for column year_opened in track) ? Hit Return > "
read -r RESP
#
# Find the id of "year_opened" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems
TRACK_YEAR_OPENED=$(curl --location --request GET "${REST_REQ}" \
                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                         --header 'Accept: application/json' | jq '.items[] | select (.name == "year_opened") | "\(.id)" ')
TRACK_YEAR_OPENED=$(echo "${TRACK_YEAR_OPENED}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.YEAR_OPENED is ${TRACK_YEAR_OPENED}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column year_opened
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/bagItems/${TRACK_YEAR_OPENED}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_TRACK}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Year_Opened\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Track.Year_Opened"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_TRACK_YEAR_OPENED=$(jq '(.id)' out.txt)
BACKEND_TRACK_YEAR_OPENED=$(echo "${BACKEND_TRACK_YEAR_OPENED}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for track.year_opened: ${BACKEND_TRACK_YEAR_OPENED}"
echo -e "------------------------------"
echo -en "Moving on (to PK creation on track.Track_ID) ? Hit Return > "
read -r RESP
#
# Create PK, with reference on Track_ID (${BACKEND_TRACK_ID})
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/backendMappings/${BACKEND_ENTITY_TRACK}/primaryKeyColumns
# PAYLOAD_3="{ \"columnMapping\": { \"id\": \"${BACKEND_TRACK_ID}\", \"type\": \"SQL\" } }",
#
# Use columnName, not columnMapping
PAYLOAD_3="{ \"columnName\" : \"Track_ID\" }",
echo -e "------------------------------"
echo -e "Create PK, with reference on Track_ID (${BACKEND_TRACK_ID})"
echo -e "Using ${REST_REQ}"
echo -e "With payload ${PAYLOAD_3}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_3}" | jq > out.txt
# cat out.txt
PK_TRACK_ID=$(jq '(.id)' out.txt)
PK_TRACK_ID=$(echo "${PK_TRACK_ID}" | tr -d '"')
#
# TODO FK (At the end)
#
#  End of Track
#
##############################
#
# RACE Entity
#
# Will need attributes race_id, name, class, date, track_id
#
echo -en "Create BackendEntityMapping for 'race' ? > "
read -r RESP
if [[ ${RESP} =~ ^(yes|y|Y)$ ]]
then
  #
  # Create BackendEntityMapping for table 'race'
  #
  PAYLOAD_1="{
    \"name\": \"RACE_MAPPING\",
    \"type\": \"SQL\",
    \"databaseName\": \"race_track\",
    \"tableName\": \"race\",
    \"defaultSelectList\": null,
    \"defaultWhere\": null,
    \"defaultOrderBy\": null,
    \"defaultLimit\": null
  }"
  REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/backendMappings
  echo -e "------------------------------"
  echo -e "Using ${REST_REQ}"
  echo -e "------------------------------"
  curl --location --request POST "${REST_REQ}" \
       --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
       --header 'Accept: application/json' \
       --header 'Content-Type: application/json' \
       --data-raw "${PAYLOAD_1}" | jq > out.txt
  #
  # cat out.txt
    BACKEND_ENTITY_RACE=$(jq '(.id)' out.txt)
    BACKEND_ENTITY_RACE=$(echo "${BACKEND_ENTITY_RACE}" | tr -d '"')
else
  echo -e "------------------------------"
  echo -e "Skipping Insert"
  echo -e "------------------------------"
  # http://100.111.136.104:9990/management-api/v1/bots/0222F7BD-8067-4752-BCC6-EB4C50D7CF61/entities/58F11EA1-158E-4BB9-82E0-F7B4D3E9CCB9/backendMappings
  REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/backendMappings
  curl --location --request GET "${REST_REQ}" \
       --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
       --header 'Accept: application/json' > out.txt
#    BACKEND_ENTITY_RACE=$(cat out.txt | jq '.items[] | select (.entity.name == "race") | "\(.id)"'  | jq --slurp '.[0]')
    BACKEND_ENTITY_RACE=$(jq '.items[] | select (.entity.name == "race") | "\(.id)"' out.txt | jq --slurp '.[0]')
    BACKEND_ENTITY_RACE=$(echo "${BACKEND_ENTITY_RACE}" | tr -d '"')
fi
echo -e "------------------------------"
echo -e "Backend Mapping for entity 'race' ${BACKEND_ENTITY_RACE}"
echo -e "------------------------------"
#
echo -en "Moving on (to Querying the backendMappings for 'race') ? Hit Return > "
read -r RESP
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/backendMappings
echo -e "------------------------------"
echo -e "Querying the backendMappings for 'race'"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request GET "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' | jq
#
echo -en "Moving on (to Create AttributeBackendMapping for column Race_ID) ? Hit Return > "
read -r RESP
######## race.race_id
# Find the id of "race_id" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems
RACE_RACE_ID=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "race_id") | "\(.id)" ')
RACE_RACE_ID=$(echo "${RACE_RACE_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.RACE_ID is ${RACE_RACE_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Race_ID
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems/${RACE_RACE_ID}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_RACE}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Race_ID\"
}"
# PAYLOAD_2="{ \"type\": \"SQL\", \"entityMapping\" : { \"id\" : \"${RACE_ENTITY_ID}\", \"type\": \"SQL\" }, \"columnName\": \"Race_ID\" }"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Race_ID"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_ID=$(jq '(.id)' out.txt)
BACKEND_RACE_ID=$(echo "${BACKEND_RACE_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for race.Race_ID: ${BACKEND_RACE_ID}"
echo -e "------------------------------"
######## race.name
# Find the id of "name" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems
RACE_NAME=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "name") | "\(.id)" ')
RACE_NAME=$(echo "${RACE_NAME}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.NAME is ${RACE_NAME}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Name
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems/${RACE_NAME}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_RACE}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Name\"
}"
# PAYLOAD_2="{ \"type\": \"SQL\", \"entityMapping\" : { \"id\" : \"${RACE_ENTITY_ID}\", \"type\": \"SQL\" }, \"columnName\": \"Race_ID\" }"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Name"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_NAME=$(jq '(.id)' out.txt)
BACKEND_RACE_NAME=$(echo "${BACKEND_RACE_NAME}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for race.Name: ${BACKEND_RACE_NAME}"
echo -e "------------------------------"
######## race.class
# Find the id of "class" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems
RACE_CLASS=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "class") | "\(.id)" ')
RACE_CLASS=$(echo "${RACE_CLASS}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.CLASS is ${RACE_CLASS}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Class
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems/${RACE_CLASS}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_RACE}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Race_Class\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Race_Class"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_CLASS=$(jq '(.id)' out.txt)
BACKEND_RACE_CLASS=$(echo "${BACKEND_RACE_CLASS}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for race.Class: ${BACKEND_RACE_CLASS}"
echo -e "------------------------------"
######## race.date
# Find the id of "date" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems
RACE_DATE=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "date") | "\(.id)" ')
RACE_DATE=$(echo "${RACE_DATE}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.DATE is ${RACE_DATE}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Date
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems/${RACE_DATE}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
    \"entityMapping\" : { \"id\" : \"${BACKEND_ENTITY_RACE}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Race_Date\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Race_Date"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_DATE=$(jq '(.id)' out.txt)
BACKEND_RACE_DATE=$(echo "${BACKEND_RACE_DATE}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for race.Date: ${BACKEND_RACE_DATE}"
echo -e "------------------------------"
######## race.track_id
# Find the id of "track_id" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems
RACE_TRACK_ID=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "track_id") | "\(.id)" ')
RACE_TRACK_ID=$(echo "${RACE_TRACK_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.TRACK_ID is ${RACE_TRACK_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Track_ID
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/bagItems/${RACE_TRACK_ID}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_RACE}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Track_ID\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column Track_ID"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_TRACK_ID=$(jq '(.id)' out.txt)
BACKEND_RACE_TRACK_ID=$(echo "${BACKEND_RACE_TRACK_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for race.Track_ID: ${BACKEND_RACE_TRACK_ID}"
echo -e "------------------------------"
# PK
echo -en "Moving on (to PK creation on race.Race_ID) ? Hit Return > "
read -r RESP
#
# Create PK, with reference on Race_ID (${BACKEND_RACE_ID})
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/backendMappings/${BACKEND_ENTITY_RACE}/primaryKeyColumns
# POST /management-api/v1/bots/{{botId}}/entities/{{entityId}}/bagItems/{{attributeId}}/backendMappings/{{backendMappingId}}/primaryKeyColumns
# PAYLOAD_3="{ \"columnMapping\": { \"id\": \"${BACKEND_RACE_ID}\", \"type\": \"SQL\" }, \"columnName\" : \"Race_ID\" }",
PAYLOAD_3="{ \"columnName\" : \"Race_ID\" }",
# PAYLOAD_3="{ \"columnMapping\": { \"id\": \"${BACKEND_RACE_ID}\", \"type\": \"SQL\" } }",
echo -e "------------------------------"
echo -e "Create PK, with reference on Race_ID (${BACKEND_RACE_ID})"
echo -e "Using ${REST_REQ}"
echo -e "With payload ${PAYLOAD_3}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_3}" | jq > out.txt
# cat out.txt
PK_RACE_ID=$(jq '(.id)' out.txt)
PK_RACE_ID=$(echo "${PK_RACE_ID}" | tr -d '"')
#
# Now, foreign keys
#
echo -en "Moving on (Creating Foreign Keys) ? Hit Return > "
read -r RESP
#
# 1 - From race.track_id to track.track_id
#
echo -e "------------------------------"
echo -e "Creating FK from race.track_id to track.track_id"
echo -e "------------------------------"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/backendMappings/${BACKEND_ENTITY_RACE}/foreignKeys
PAYLOAD="{ \"name\": \"track\" }"
echo -e "------------------------------"
echo -e "Create FK, from race.track_id to track.track_id"
echo -e "Using ${REST_REQ}"
echo -e "With payload ${PAYLOAD}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
# cat out.txt
RACE_FK_TRACK=$(jq '(.id)' out.txt)
RACE_FK_TRACK=$(echo "${RACE_FK_TRACK}" | tr -d '"')
# Add columns in the FK
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/backendMappings/${BACKEND_ENTITY_RACE}/foreignKeys/${RACE_FK_TRACK}/columns
PAYLOAD="{
    \"sequenceNr\" : 1,
    \"type\" : \"COLUMN_NAME\",
    \"columnMapping\" : {
        \"id\" : \"${BACKEND_RACE_TRACK_ID}\",
        \"type\" : \"SQL\"
    },
    \"targetTableMapping\" : {
        \"id\" : \"${BACKEND_ENTITY_TRACK}\",
        \"type\" : \"SQL\"
    },
    \"targetColumnName\" : \"Track_ID\"
}"
echo -e "------------------------------"
echo -e "Create FK, Adding columns"
echo -e "Using ${REST_REQ}"
echo -e "With payload ${PAYLOAD}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq
# cat out.txt
echo -en "Moving on (Creating 2nd Foreign Key) ? Hit Return > "
read -r RESP
#
# 2 - From track.track_id to race.track_id
#
echo -e "------------------------------"
echo -e "Creating FK from track.track_id to race.track_id"
echo -e "------------------------------"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/backendMappings/${BACKEND_ENTITY_TRACK}/foreignKeys
PAYLOAD="{ \"name\": \"races\" }"
echo -e "------------------------------"
echo -e "Create FK, from track.track_id to race.track_id"
echo -e "Using ${REST_REQ}"
echo -e "With payload ${PAYLOAD}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
# cat out.txt
TRACK_FK_RACE=$(jq '(.id)' out.txt)
TRACK_FK_RACE=$(echo "${TRACK_FK_RACE}" | tr -d '"')
# Add columns in the FK
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/backendMappings/${BACKEND_ENTITY_TRACK}/foreignKeys/${TRACK_FK_RACE}/columns
PAYLOAD="{
    \"sequenceNr\" : 1,
    \"type\" : \"COLUMN_NAME\",
    \"columnMapping\" : {
        \"id\" : \"${BACKEND_TRACK_ID}\",
        \"type\" : \"SQL\"
    },
    \"targetTableMapping\" : {
        \"id\" : \"${BACKEND_ENTITY_RACE}\",
        \"type\" : \"SQL\"
    },
    \"targetColumnName\" : \"Track_ID\"
}"
echo -e "------------------------------"
echo -e "Create FK, Adding columns"
echo -e "Using ${REST_REQ}"
echo -e "With payload ${PAYLOAD}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq
# cat out.txt
