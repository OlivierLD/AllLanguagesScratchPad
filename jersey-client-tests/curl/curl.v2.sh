#!/bin/bash
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
# 9E07BBDC-816A-44A8-B458-E53CF8868E65
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
# Create COMPOSITEENTITY track
#
PAYLOAD="{
  \"name\": \"track\",
  \"type\": \"COMPOSITEENTITY\",
  \"description\": \"\"
}"
curl --location --request POST "${REST_REQ}" \
                       --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                       --header 'Accept: application/json' \
                       --header 'Content-Type: application/json' \
                       --data-raw "${PAYLOAD}" > out.txt
echo -e "Hit [return]"
read -r RESP
#
TRACK_ENTITY_ID=$(jq '.id' out.txt)
TRACK_ENTITY_ID=$(echo "${TRACK_ENTITY_ID}" | tr -d '"')
TRACK_NL_MAPPING_ID=$(jq '.entityNLMapping.id' out.txt)
TRACK_NL_MAPPING_ID=$(echo "${TRACK_NL_MAPPING_ID}" | tr -d '"')
TRACK_PRESENTATION_MAPPING_ID=$(jq '.entityPresentationMapping.id' out.txt)
TRACK_PRESENTATION_MAPPING_ID=$(echo "${TRACK_PRESENTATION_MAPPING_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK is ${TRACK_ENTITY_ID}"
echo -e "------------------------------"
#
# Create COMPOSITEENTITY race
#
PAYLOAD="{
  \"name\": \"race\",
  \"type\": \"COMPOSITEENTITY\",
  \"description\": \"\"
}"
curl --location --request POST "${REST_REQ}" \
                       --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                       --header 'Accept: application/json' \
                       --header 'Content-Type: application/json' \
                       --data-raw "${PAYLOAD}" > out.txt
#
RACE_ENTITY_ID=$(jq '.id' out.txt)
RACE_ENTITY_ID=$(echo "${RACE_ENTITY_ID}" | tr -d '"')
RACE_NL_MAPPING_ID=$(jq '.entityNLMapping.id' out.txt)
RACE_NL_MAPPING_ID=$(echo "${RACE_NL_MAPPING_ID}" | tr -d '"')
RACE_PRESENTATION_MAPPING_ID=$(jq '.entityPresentationMapping.id' out.txt)
RACE_PRESENTATION_MAPPING_ID=$(echo "${RACE_PRESENTATION_MAPPING_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE is ${RACE_ENTITY_ID}"
echo -e "------------------------------"
#
# Add synonyms for entities
#
#########################################
echo -en "Add synonyms to entities ? [Hit Return] "
read -r RESP

#
# 1 - Track
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/nlMappings/${TRACK_NL_MAPPING_ID}
PAYLOAD="{
    \"name\": \"DEFAULT\",
    \"synonyms\": [{
        \"canonicalName\": \"Speedway\",
        \"synonyms\": [
                \"Race Track\",
                \"Automotive Race Track\",
                \"Racing Track\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for track"
echo -e "------------------------------"
#
# 2 - Race
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/nlMappings/${RACE_NL_MAPPING_ID}
PAYLOAD="{
    \"name\": \"DEFAULT\",
    \"synonyms\": [{
        \"canonicalName\": \"Racing\",
        \"synonyms\": [
                \"Car Racing\",
                \"Automotive Race\",
                \"Racing Competition\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for race"
echo -e "------------------------------"


#
# List attributes for one entity
# http://100.111.136.104:9990/management-api/v1/bots/A624FF72-D030-4F26-890E-9C80827CB129/entities/B220B523-BB8D-4AD6-9554-6C6D38F9C8F0/attributes
#
#########################################
#
# TRACK Entity.
#
# Need track_id, name, location, seating, year_opened
# PK on track_id
# FK track_id -> race.track_id
#
echo -en "Create EntityBackendMapping for 'track' ? [Hit Return] "
read -r RESP
#
# Create EntityBackendMapping for entity 'track'
# name contains the linked Entity Name!!! Was TRACK_MAPPING
# name: track
PAYLOAD_1="{
  \"name\": \"race\",
  \"type\": \"SQL\",
  \"dataServiceName\": \"race_track\",
  \"tableName\": \"track\",
  \"defaultSelectList\": null,
  \"defaultWhere\": null,
  \"defaultOrderBy\": null,
  \"defaultLimit\": null,
  \"primaryKey\": [ \"Track_ID\" ]
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/backendMappings
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
BACKEND_ENTITY_TRACK=$(jq '(.id)' out.txt)
BACKEND_ENTITY_TRACK=$(echo "${BACKEND_ENTITY_TRACK}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created backendMapping for track ${BACKEND_ENTITY_TRACK}"
echo -e "------------------------------"
#
# Create track attributes
# track_id, name, location, seating, year_opened
#
# 1 - track_id
PAYLOAD="{
  \"name\": \"track_id\",
  \"type\": \"NUMBER\",
  \"sequenceNr\": 1
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute track.traci_id
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Track ID\",
        \"synonyms\": [
                \"Track-ID\",
                \"Track_ID\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out1.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute track.traci_id"
echo -e "------------------------------"


#
# 2 - name
PAYLOAD="{
  \"name\": \"name\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 2
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute track.name
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Track Name\",
        \"synonyms\": [
                \"Track-Name\",
                \"Track_Name\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute track.name"
echo -e "------------------------------"


#
# 3 - location
PAYLOAD="{
  \"name\": \"location\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 3
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute track.location
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Location\",
        \"synonyms\": [
                \"Track-Location\",
                \"Track Location\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute track.location"
echo -e "------------------------------"


#
# 4 - seating
PAYLOAD="{
  \"name\": \"seating\",
  \"type\": \"NUMBER\",
  \"sequenceNr\": 4
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute track.seating
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Seating\",
        \"synonyms\": [
                \"Track-Seating\",
                \"Track Seating\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute track.seating"
echo -e "------------------------------"


#
# 5 - year_opened
PAYLOAD="{
  \"name\": \"year_opened\",
  \"type\": \"NUMBER\",
  \"sequenceNr\": 5
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute track.year_opened
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Year Opened\",
        \"synonyms\": [
                \"Track-Year-Opened\",
                \"Track Year Opened\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute track.year_opened"
echo -e "------------------------------"


#
# 6 - races (link) Attribute. With entityUsage. TODO check multiValue
PAYLOAD="{
  \"name\": \"races\",
  \"type\": \"COMPOSITE_ENTITY\",
  \"multiValue\": true,
  \"sequenceNr\": 6,
  \"entityUsage\": {
    \"id\": \"${RACE_ENTITY_ID}\"
  }
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}\nwith\n${PAYLOAD}"
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
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' > out.txt
#
# jq . out.txt
echo -e "---------------------------"
echo -e "Created Attributes in track:"
jq '.items[] | [.name, .type ] | join(" type ")' out.txt
echo -en "[Hit Return] "
read -r RESP
#
# Create race attributes
# race_id, name, class, date, track_id
#
# 1 - race_id
PAYLOAD="{
  \"name\": \"race_id\",
  \"type\": \"NUMBER\",
  \"sequenceNr\": 1
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.race_id
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Race ID\",
        \"synonyms\": [
                \"Race-ID\",
                \"Race_ID\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.race_id"
echo -e "------------------------------"


# 2 - name
PAYLOAD="{
  \"name\": \"name\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 2
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.name
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Race Name\",
        \"synonyms\": [
                \"Race-Name\",
                \"Race_Name\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.name"
echo -e "------------------------------"


# 3 - class
PAYLOAD="{
  \"name\": \"class\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 3
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.class
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Race Class\",
        \"synonyms\": [
                \"Race-Class\",
                \"Race_Class\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.class"
echo -e "------------------------------"


# 4 - date
PAYLOAD="{
  \"name\": \"date\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 4
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.date
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Race Date\",
        \"synonyms\": [
                \"Race-Date\",
                \"Race_Date\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.date"
echo -e "------------------------------"


# 5 - track_id
PAYLOAD="{
  \"name\": \"track_id\",
  \"type\": \"NUMBER\",
  \"sequenceNr\": 5
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.track_id
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Track ID\",
        \"synonyms\": [
                \"Track-ID\",
                \"Track_ID\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.track_id"
echo -e "------------------------------"


# 6 - track relation
PAYLOAD="{
  \"name\": \"track\",
  \"type\": \"COMPOSITE_ENTITY\",
  \"sequenceNr\": 6,
  \"entityUsage\": {
    \"id\": \"${TRACK_ENTITY_ID}\"
  }
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt


# 7 - num_name_date sqlExpression
PAYLOAD="{
  \"name\": \"num_name_date\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 7
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt


# 8 - cost column expansion
PAYLOAD="{
  \"name\": \"cost\",
  \"type\": \"NUMBER\",
  \"sequenceNr\": 8
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.cost
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Cost\",
        \"synonyms\": [
                \"Costing\",
                \"Race Cost\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.cost"
echo -e "------------------------------"


#
# Entities for column expansions
#
# 9 - year
PAYLOAD="{
  \"name\": \"year\",
  \"type\": \"ENTITY\",
  \"sequenceNr\": 9
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.year
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Year\",
        \"synonyms\": [
                \"Race-Year\",
                \"Race_Year\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.year"
echo -e "------------------------------"


# 10 - quarter
PAYLOAD="{
  \"name\": \"quarter\",
  \"type\": \"ENTITY\",
  \"sequenceNr\": 10
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.quarter
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Quarter\",
        \"synonyms\": [
                \"Race-Quarter\",
                \"Race_Quarter\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.quarter"
echo -e "------------------------------"


# 11 - period
PAYLOAD="{
  \"name\": \"period\",
  \"type\": \"ENTITY\",
  \"sequenceNr\": 11
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.period
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Period\",
        \"synonyms\": [
                \"Race-Period\",
                \"Race_Period\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.period"
echo -e "------------------------------"


# 12 - type
PAYLOAD="{
  \"name\": \"type\",
  \"type\": \"ENTITY\",
  \"sequenceNr\": 12
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.type
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Type\",
        \"synonyms\": [
                \"Race-Type\",
                \"Race_Type\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.type"
echo -e "------------------------------"


# 13 - race_stuff column expansion
PAYLOAD="{
  \"name\": \"race_stuff\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 13
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.race_stuff
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Race Stuff\",
        \"synonyms\": [
                \"Race-Stuff\",
                \"Race_Stuff\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.race_stuff"
echo -e "------------------------------"


# 14 - stuff - for column expansion
PAYLOAD="{
  \"name\": \"stuff\",
  \"type\": \"ENTITY\",
  \"sequenceNr\": 14
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Add synonyms for attribute race.stuff
#
ATTRIBUTE_ID=$(jq '.id' out.txt)
ATTRIBUTE_ID=$(echo "${ATTRIBUTE_ID}" | tr -d '"')
ATTRIBUTE_NL_MAPPING_ID=$(jq '.attributeNLMapping.id' out.txt)
ATTRIBUTE_NL_MAPPING_ID=$(echo "${ATTRIBUTE_NL_MAPPING_ID}" | tr -d '"')
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${ATTRIBUTE_ID}/nlMappings/${ATTRIBUTE_NL_MAPPING_ID}
PAYLOAD="{
    \"requestable\": true,
    \"informable\": true,
    \"aggregatable\": false,
    \"synonyms\": [{
        \"canonicalName\": \"Stuff\",
        \"synonyms\": [
                \"Some Stuff\",
                \"Something\"
            ],
        \"nativeLanguageTag\": \"en\"
    }]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "------------------------------"
echo -e "Added synonyms for attribute race.stuff"
echo -e "------------------------------"


#
# Display created attributes
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' > out.txt
#
# jq . out.txt
echo -e "---------------------------"
echo -e "Created Attributes in race:"
jq '.items[] | [.name, .type ] | join(" type ")' out.txt
echo -en "[Hit Return] "
read -r RESP
#
#
#
echo -en "Moving on (to Create AttributeBackendMapping for column track_id in track) ? [Hit Return] "
read -r RESP
########### track.track_id
# Find the id of "track_id" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
                      --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                      --header 'Accept: application/json' > out.txt
TRACK_TRACK_ID=$(jq '.items[] | select (.name == "track_id") | "\(.id)" ' out.txt)
TRACK_TRACK_ID=$(echo "${TRACK_TRACK_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.TRACK_ID is ${TRACK_TRACK_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column track_id
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${TRACK_TRACK_ID}/backendMappings
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
echo -en "Moving on (to Create AttributeBackendMapping for column name in track) ? [Hit Return] "
read -r RESP
#
# Find the id of "name" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
TRACK_NAME=$(curl --location --request GET "${REST_REQ}" \
                  --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                  --header 'Accept: application/json' | jq '.items[] | select (.name == "name") | "\(.id)" ')
TRACK_NAME=$(echo "${TRACK_NAME}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.NAME is ${TRACK_NAME}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column name
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${TRACK_NAME}/backendMappings
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
echo -en "Moving on (to Create AttributeBackendMapping for column location in track) ? [Hit Return] "
read -r RESP
#
# Find the id of "location" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
TRACK_LOCATION=$(curl --location --request GET "${REST_REQ}" \
                      --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                      --header 'Accept: application/json' | jq '.items[] | select (.name == "location") | "\(.id)" ')
TRACK_LOCATION=$(echo "${TRACK_LOCATION}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.LOCATION is ${TRACK_LOCATION}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column location
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${TRACK_LOCATION}/backendMappings
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
echo -en "Moving on (to Create AttributeBackendMapping for column seating in track) ? [Hit Return] "
read -r RESP
#
# Find the id of "location" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
TRACK_SEATING=$(curl --location --request GET "${REST_REQ}" \
                     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                     --header 'Accept: application/json' | jq '.items[] | select (.name == "seating") | "\(.id)" ')
TRACK_SEATING=$(echo "${TRACK_SEATING}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.SEATING is ${TRACK_SEATING}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column seating
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${TRACK_SEATING}/backendMappings
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
echo -en "Moving on (to Create AttributeBackendMapping for column year_opened in track) ? [Hit Return] "
read -r RESP
#
# Find the id of "year_opened" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
TRACK_YEAR_OPENED=$(curl --location --request GET "${REST_REQ}" \
                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                         --header 'Accept: application/json' | jq '.items[] | select (.name == "year_opened") | "\(.id)" ')
TRACK_YEAR_OPENED=$(echo "${TRACK_YEAR_OPENED}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.YEAR_OPENED is ${TRACK_YEAR_OPENED}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column year_opened
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${TRACK_YEAR_OPENED}/backendMappings
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
#
# Creating link
# Find the id of "races" in the "track" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes
TRACK_RACES=$(curl --location --request GET "${REST_REQ}" \
                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                         --header 'Accept: application/json' | jq '.items[] | select (.name == "races") | "\(.id)" ')
TRACK_RACES=$(echo "${TRACK_RACES}" | tr -d '"')
echo -e "------------------------------"
echo -e "TRACK.RACES is ${TRACK_RACES}"
echo -e "------------------------------"
# Create AttributeBackendMapping for column races
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/attributes/${TRACK_RACES}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_TRACK}\",
    \"type\": \"SQL\"
  },
  \"foreignKey\": [ \"Track_ID\" ]
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for link Track.races"
echo -e "Using ${REST_REQ} \nwith\n${PAYLOAD_2}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_TRACK_RACES=$(jq '(.id)' out.txt)
BACKEND_TRACK_RACES=$(echo "${BACKEND_TRACK_RACES}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for track.races: ${BACKEND_TRACK_RACES}"
echo -e "------------------------------"
echo -en "Done with track. [Hit Return] "
read -r RESP
#
#  End of Track
#
##############################
#
# RACE Entity
#
# Will need attributes race_id, name, class, date, track_id
#
# echo -en "Create BackendEntityMapping for 'race' ? > "
# read -r RESP
echo -en "Create BackendEntityMapping for 'race' ? [Hit Return] > "
RESP=y
#
if [[ ${RESP} =~ ^(yes|y|Y)$ ]]; then
  #
  # Create BackendEntityMapping for table 'race'
  # name contains the Entity Name!! Was RACE_MAPPING
  # Q for Diego: where to put that?
  PAYLOAD_1="{
    \"name\": \"track\",
    \"type\": \"SQL\",
    \"dataServiceName\": \"race_track\",
    \"tableName\": \"race\",
    \"defaultSelectList\": null,
    \"defaultWhere\": null,
    \"defaultOrderBy\": [ \"class\",  \"name ASC\" ],
    \"defaultLimit\": null,
    \"primaryKey\": [ \"Race_ID\" ]
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
echo -en "Moving on (to Querying the backendMappings for 'race') ? [Hit Return] "
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
echo -en "Moving on (to Create AttributeBackendMapping for column Race_ID) ? [Hit Return] "
read -r RESP
######## race.race_id
# Find the id of "race_id" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
RACE_RACE_ID=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "race_id") | "\(.id)" ')
RACE_RACE_ID=$(echo "${RACE_RACE_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.RACE_ID is ${RACE_RACE_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Race_ID
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${RACE_RACE_ID}/backendMappings
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
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
RACE_NAME=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "name") | "\(.id)" ')
RACE_NAME=$(echo "${RACE_NAME}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.NAME is ${RACE_NAME}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Name
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${RACE_NAME}/backendMappings
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
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
RACE_CLASS=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "class") | "\(.id)" ')
RACE_CLASS=$(echo "${RACE_CLASS}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.CLASS is ${RACE_CLASS}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Class
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${RACE_CLASS}/backendMappings
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
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
RACE_DATE=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "date") | "\(.id)" ')
RACE_DATE=$(echo "${RACE_DATE}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.DATE is ${RACE_DATE}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Date
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${RACE_DATE}/backendMappings
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
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
RACE_TRACK_ID=$(curl --location --request GET "${REST_REQ}" --header "Bots-TenantId: ${BOTS_TENANT_ID}" --header 'Accept: application/json' | jq '.items[] | select (.name == "track_id") | "\(.id)" ')
RACE_TRACK_ID=$(echo "${RACE_TRACK_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.TRACK_ID is ${RACE_TRACK_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column Track_ID
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${RACE_TRACK_ID}/backendMappings
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
# Link (FK)
#
# Creating link
# Find the id of "track" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
RACE_TRACK=$(curl --location --request GET "${REST_REQ}" \
                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                         --header 'Accept: application/json' | jq '.items[] | select (.name == "track") | "\(.id)" ')
RACE_TRACK=$(echo "${RACE_TRACK}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.TRACK is ${RACE_TRACK}"
echo -e "------------------------------"
# Create AttributeBackendMapping for column races
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${RACE_TRACK}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_RACE}\",
    \"type\": \"SQL\"
  },
  \"foreignKey\": [ \"Track_ID\" ]
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for link race.track"
echo -e "Using ${REST_REQ} \nwith\n${PAYLOAD_2}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_TRACK=$(jq '(.id)' out.txt)
BACKEND_RACE_TRACK=$(echo "${BACKEND_RACE_TRACK}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for race.track: ${BACKEND_RACE_TRACK}"
echo -e "------------------------------"
#
# cat out.txt
#
# TODO Attributes for sqlExpressions and column expansions
#
# Find the id of "num_name_date" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
RACE_NNDATE=$(curl --location --request GET "${REST_REQ}" \
                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                         --header 'Accept: application/json' | jq '.items[] | select (.name == "num_name_date") | "\(.id)" ')
RACE_NNDATE=$(echo "${RACE_NNDATE}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.NUM_NAME_DATE is ${RACE_NNDATE}"
echo -e "------------------------------"
# Create AttributeBackendMapping for column cost
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${RACE_NNDATE}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_RACE}\",
    \"type\": \"SQL\"
  },
  \"sqlExpression\": \"'Race #' || TO_CHAR(RACE_ID) || ', ' || RACE.NAME || ', on ' || RACE_DATE\"
}"
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for link race.num_name_date"
echo -e "Using ${REST_REQ} \nwith\n${PAYLOAD_2}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_NND=$(jq '(.id)' out.txt)
BACKEND_RACE_NND=$(echo "${BACKEND_RACE_NND}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for race.num_name_date: ${BACKEND_RACE_NND}"
echo -e "------------------------------"
#
# Find the id of "cost" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
RACE_COST=$(curl --location --request GET "${REST_REQ}" \
                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                         --header 'Accept: application/json' | jq '.items[] | select (.name == "cost") | "\(.id)" ')
RACE_COST=$(echo "${RACE_COST}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.COST is ${RACE_COST}"
echo -e "------------------------------"
# Create AttributeBackendMapping for column cost
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${RACE_COST}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_RACE}\",
    \"type\": \"SQL\"
  },
  \"columnExpansions\": [ \"\${year}_\${quarter}_CST\", \"\${period}_\${type}_CST\" ]
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for link race.cost"
echo -e "Using ${REST_REQ} \nwith\n${PAYLOAD_2}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_COST=$(jq '(.id)' out.txt)
BACKEND_RACE_COST=$(echo "${BACKEND_RACE_COST}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for race.cost: ${BACKEND_RACE_COST}"
echo -e "------------------------------"
# race_stuff
# Find the id of "race_stuff" in the "race" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes
RACE_RACE_STUFF=$(curl --location --request GET "${REST_REQ}" \
                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                         --header 'Accept: application/json' | jq '.items[] | select (.name == "race_stuff") | "\(.id)" ')
RACE_RACE_STUFF=$(echo "${RACE_RACE_STUFF}" | tr -d '"')
echo -e "------------------------------"
echo -e "RACE.RACE_STUFF is ${RACE_RACE_STUFF}"
echo -e "------------------------------"
# Create AttributeBackendMapping for column cost
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/attributes/${RACE_RACE_STUFF}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_RACE}\",
    \"type\": \"SQL\"
  },
  \"columnExpansions\": [ \"RACE_\${stuff}\" ]
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for link race.cost"
echo -e "Using ${REST_REQ} \nwith\n${PAYLOAD_2}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_RACE_TACE_STUFF=$(jq '(.id)' out.txt)
BACKEND_RACE_TACE_STUFF=$(echo "${BACKEND_RACE_TACE_STUFF}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for race.race_stuff: ${BACKEND_RACE_TACE_STUFF}"
echo -e "------------------------------"


#
# Update entity Presentation Mapping
#
#########################################

#
# 1 - Track
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${TRACK_ENTITY_ID}/presentationMappings/${TRACK_PRESENTATION_MAPPING_ID}
PAYLOAD="{
    \"name\": \"DEFAULT\",
    \"deepLinkUrl\" : \"http://localhost/ODA/OMRL2SQL/Track\",
    \"deepLinkLabel\": \"Track URL\",
    \"defaultAttributes\": [
      {
        \"entityAttribute\" :{
          \"id\" : \"${TRACK_NAME}\"
        }
      },
      {
        \"entityAttribute\" :{
          \"id\" : \"${TRACK_LOCATION}\"
        }
      },
      {
        \"entityAttribute\" :{
          \"id\" : \"${TRACK_YEAR_OPENED}\"
        }
      }
    ],
    \"minimumAttributes\": [
      {
        \"entityAttribute\" :{
          \"id\" : \"${TRACK_NAME}\"
        }
      },
      {
        \"entityAttribute\" :{
          \"id\" : \"${TRACK_LOCATION}\"
        }
      }
    ]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "--------------------------------------"
echo -e "Updated Presentation Mapping for track"
echo -e "--------------------------------------"

#
# 2 - Race
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${RACE_ENTITY_ID}/presentationMappings/${RACE_PRESENTATION_MAPPING_ID}
PAYLOAD="{
    \"name\": \"DEFAULT\",
    \"deepLinkUrl\" : \"http://localhost/ODA/OMRL2SQL/Race\",
    \"deepLinkLabel\": \"Race URL\",
    \"defaultAttributes\": [
      {
        \"entityAttribute\" :{
          \"id\" : \"${RACE_NAME}\"
        }
      },
      {
        \"entityAttribute\" :{
          \"id\" : \"${RACE_DATE}\"
        }
      },
      {
        \"entityAttribute\" :{
          \"id\" : \"${RACE_CLASS}\"
        }
      }
    ],
    \"minimumAttributes\": [
      {
        \"entityAttribute\" :{
          \"id\" : \"${RACE_NAME}\"
        }
      },
      {
        \"entityAttribute\" :{
          \"id\" : \"${RACE_DATE}\"
        }
      }
    ]
}"
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request PUT "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" | jq > out.txt
echo -e "-------------------------------------"
echo -e "Updated Presentation Mapping for race"
echo -e "-------------------------------------"

