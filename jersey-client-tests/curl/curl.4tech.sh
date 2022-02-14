#!/bin/bash
#
#################################################################################
# Creating Diego's properties for Backend Mappings, on tetra_tech (Cost, AR)    #
#################################################################################
#
# jq cheat-sheet: https://lzone.de/cheat-sheet/jq
# Some of my jq samples: https://confluence.oraclecorp.com/confluence/display/AARCH/Playground#Playground-EditaDecisionService
#
# Diego's REST EndPoints: https://confluence.oraclecorp.com/confluence/display/IBS/Design+Time+Implementation+for+C2SQL+MVP#DesignTimeImplementationforC2SQLMVP-ForeignKey
#
ODA_INSTANCE=100.111.136.104
#
BOT_ID=3AD3348C-5A60-4D8F-A36C-C6D41A0AF08F
# D832F2DE-9505-4F2F-9364-4C978D8550FC
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
echo -e "OK, let's go"
#
# REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/29660F52-305A-449E-AF67-C7D91C3140E2
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities
#
# Create COMPOSITEENTITY Cost
#
PAYLOAD="{
  \"name\": \"Cost\",
  \"type\": \"COMPOSITEENTITY\",
  \"description\": \"\"
}"
curl --location --request POST "${REST_REQ}" \
                       --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                       --header 'Accept: application/json' \
                       --header 'Content-Type: application/json' \
                       --data-raw "${PAYLOAD}" > out.txt
#
# cat out.txt | jq
#
COST_ENTITY_ID=$(jq '.id' out.txt)
echo -e "CostEntityID is ${COST_ENTITY_ID}"
#
COST_ENTITY_ID=$(echo "${COST_ENTITY_ID}" | tr -d '"')
if [[ "${COST_ENTITY_ID}" == "null" ]]
then
  echo -e "Oops..."
  cat out.txt | jq
  echo -e "Hit [return]"
  read -r RESP
else
  echo -e "------------------------------"
  echo -e "COST is ${COST_ENTITY_ID}"
  echo -e "------------------------------"
fi
#
# Create COMPOSITEENTITY AR
#
PAYLOAD="{
  \"name\": \"AR\",
  \"type\": \"COMPOSITEENTITY\",
  \"description\": \"\"
}"
curl --location --request POST "${REST_REQ}" \
                       --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                       --header 'Accept: application/json' \
                       --header 'Content-Type: application/json' \
                       --data-raw "${PAYLOAD}" > out.txt
#
AR_ENTITY_ID=$(jq '.id' out.txt)
AR_ENTITY_ID=$(echo "${AR_ENTITY_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "AR is ${AR_ENTITY_ID}"
echo -e "------------------------------"
#
# List attributes for one entity
# http://100.111.136.104:9990/management-api/v1/bots/A624FF72-D030-4F26-890E-9C80827CB129/entities/B220B523-BB8D-4AD6-9554-6C6D38F9C8F0/attributes
#
#########################################
#
# AR Entity.
#
# Need ar, total_ar
# PK on ?
# FK none
#
echo -en "Create EntityBackendMapping for 'AR' ? [Hit Return] "
read -r RESP
#
# Create EntityBackendMapping for entity 'AR'
# name contains the linked Entity Name!!!
# name: AR - TODO databaseName, change to dataServiceName
PAYLOAD_1="{
  \"name\": \"AR\",
  \"type\": \"SQL\",
  \"dataServiceName\": \"tetra_tech\",
  \"tableName\": \"CSR\",
  \"defaultSelectList\": null,
  \"defaultWhere\": null,
  \"defaultOrderBy\": null,
  \"defaultLimit\": null
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/backendMappings
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
BACKEND_ENTITY_AR=$(jq '(.id)' out.txt)
BACKEND_ENTITY_AR=$(echo "${BACKEND_ENTITY_AR}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created backendMapping for AR ${BACKEND_ENTITY_AR}"
echo -e "------------------------------"
#
# Create AR attributes
# ar, total_ar
#
# 1 - ar
PAYLOAD="{
  \"name\": \"ar\",
  \"type\": \"NUMBER\",
  \"sequenceNr\": 1
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# 2 - name
PAYLOAD="{
  \"name\": \"total_ar\",
  \"type\": \"NUMBER\",
  \"sequenceNr\": 2
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Entities for column expansions. daysrange, days_over
#
# 3 - daysrange (01_30, 31_60, 61_90, 91_120). Was type: ENTITY
PAYLOAD="{
  \"name\": \"daysrange\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 3
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# 3 - days_over (120). Was type: ENTITY
PAYLOAD="{
  \"name\": \"days_over\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 4
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# echo -en "[Hit Return] "
# read -r RESP
#
# Display created attributes
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' > out.txt
#
# jq . out.txt
echo -e "---------------------------"
echo -e "Created Attributes in AR:"
jq '.items[] | [.name, .type ] | join(" type ")' out.txt
echo -en "[Hit Return] "
read -r RESP

#
# Create EntityBackendMapping for entity 'Cost'
# name contains the linked Entity Name!!!
# name: Cost - TODO databaseName, change to dataServiceName
PAYLOAD_1="{
  \"name\": \"Cost\",
  \"type\": \"SQL\",
  \"dataServiceName\": \"tetra_tech\",
  \"tableName\": \"CSR\",
  \"defaultSelectList\": null,
  \"defaultWhere\": null,
  \"defaultOrderBy\": null,
  \"defaultLimit\": null
}"
# TODO New payload?
# {
#    "name": "DEFAULT",
#    "type": "SQL",
#    "databaseName": "DB_NAME",
#    "tableName": "TABLE_NAME",
#    "sqlSelect" : "foobar expression",
#    "defaultOrderBy": [],
#    "primaryKey" : ["foo", "bar"]
# }
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/backendMappings
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
BACKEND_ENTITY_COST=$(jq '(.id)' out.txt)
BACKEND_ENTITY_COST=$(echo "${BACKEND_ENTITY_COST}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created backendMapping for Cost ${BACKEND_ENTITY_COST}"
echo -e "------------------------------"
#
# Create Cost attributes
# cost, column expansion
#
# 1 - cost
PAYLOAD="{
  \"name\": \"cost\",
  \"type\": \"NUMBER\",
  \"sequenceNr\": 1
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Entities for column expansions
#
# Find the Period_VLE id, must have been created before.
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities
echo -e "------------------------------"
echo -e "Finding Period_VLE, Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request GET "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' > out.txt
# cat out.txt | jq '.items[] | select (.name == "Period_VLE") | "\(.id)" '
PERIOD_VLE_ID=$(jq '.items[] | select (.name == "Period_VLE") | "\(.id)" ' out.txt)
PERIOD_VLE_ID=$(echo "${PERIOD_VLE_ID}" | tr -d '"')
#
echo -e "Period_VLE id: ${PERIOD_VLE_ID}"
echo -en "[Hit Return] "
read -r RESP
# 2 - period (PTD, QTD, YTD, ITD). Refers to the ID of the VLE.
PAYLOAD="{
  \"name\": \"period\",
  \"type\": \"ENTITY\",
  \"entityUsage\": {
    \"id\": \"${PERIOD_VLE_ID}\"
  },
  \"sequenceNr\": 2
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Creating period"
echo -e "Using ${REST_REQ}"
echo -e "With ${PAYLOAD}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
echo -e "---------------------------------"
cat out.txt
echo -en "[Hit Return] "
read -r RESP
#
# Find the Type_VLE id, must have been created before.
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities
echo -e "------------------------------"
echo -e "Finding Type_VLE, Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request GET "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' > out.txt
#
TYPE_VLE_ID=$(jq '.items[] | select (.name == "Type_VLE") | "\(.id)" ' out.txt)
TYPE_VLE_ID=$(echo "${TYPE_VLE_ID}" | tr -d '"')
#
echo -e "Type_VLE id: ${TYPE_VLE_ID}"
echo -en "[Hit Return] "
read -r RESP

# 3 - type (LBR, SUB, ODC, BRDN). type: ENTITY
PAYLOAD="{
  \"name\": \"type\",
  \"type\": \"ENTITY\",
  \"entityUsage\": {
    \"id\": \"${TYPE_VLE_ID}\"
  },
  \"sequenceNr\": 3
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
# Create Company attributes
# company, DB column
#
# 4 - company
PAYLOAD="{
  \"name\": \"company\",
  \"type\": \"TEXT\",
  \"sequenceNr\": 4
}"
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes
echo -e "------------------------------"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD}" > out.txt
#
# Display created attributes
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes
curl --location --request GET "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' > out.txt
#
# jq . out.txt
echo -e "---------------------------"
echo -e "Created Attributes in Cost:"
jq '.items[] | [.name, .type ] | join(" type ")' out.txt
echo -en "[Hit Return] "
read -r RESP
#
echo -en "Moving on (to Create AttributeBackendMapping for column cost in Cost) ? [Hit Return] "
read -r RESP
########### Cost.cost
# Find the id of "cost" in the "Cost" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes
COST_COST=$(curl --location --request GET "${REST_REQ}" \
                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                         --header 'Accept: application/json' | jq '.items[] | select (.name == "cost") | "\(.id)" ')
COST_COST=$(echo "${COST_COST}" | tr -d '"')
echo -e "------------------------------"
echo -e "COST.COST is ${COST_COST}"
echo -e "------------------------------"
# Create AttributeBackendMapping for column cost
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes/${COST_COST}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_COST}\",
    \"type\": \"SQL\"
  },
  \"columnExpansions\": [ \"\${period}_\${type}_CST\" ]
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column cost"
echo -e "Using ${REST_REQ} \nwith\n${PAYLOAD_2}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_COST_COST=$(jq '(.id)' out.txt)
BACKEND_COST_COST=$(echo "${BACKEND_COST_COST}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for cost.cost: ${BACKEND_COST_COST}"
echo -e "------------------------------"
#
#echo -en "Moving on (to Create AttributeBackendMapping for column cost in Cost) ? [Hit Return] "
#read -r RESP
############ Cost.cost
## Find the id of "cost" in the "Cost" Entity
#REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes
#COST_COST=$(curl --location --request GET "${REST_REQ}" \
#                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
#                         --header 'Accept: application/json' | jq '.items[] | select (.name == "cost") | "\(.id)" ')
#COST_COST=$(echo "${COST_COST}" | tr -d '"')
#echo -e "------------------------------"
#echo -e "COST.COST is ${COST_COST}"
#echo -e "------------------------------"
## Create AttributeBackendMapping for column cost
#REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes/${COST_COST}/backendMappings
#PAYLOAD_2="{
#  \"type\": \"SQL\",
#  \"entityMapping\" : {
#    \"id\" : \"${BACKEND_ENTITY_COST}\",
#    \"type\": \"SQL\"
#  },
#  \"columnExpansions\": [ \"\${period}_\${type}_CST\" ]
#}"
##
#echo -e "------------------------------"
#echo -e "Create AttributeBackendMapping for column cost"
#echo -e "Using ${REST_REQ} \nwith\n${PAYLOAD_2}"
#echo -e "------------------------------"
#curl --location --request POST "${REST_REQ}" \
#     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
#     --header 'Accept: application/json' \
#     --header 'Content-Type: application/json' \
#     --data-raw "${PAYLOAD_2}" | jq > out.txt
## cat out.txt
#BACKEND_COST_COST=$(jq '(.id)' out.txt)
#BACKEND_COST_COST=$(echo "${BACKEND_COST_COST}" | tr -d '"')
#echo -e "------------------------------"
#echo -e "Created Attribute Backend Mapping for cost.cost: ${BACKEND_COST_COST}"
#echo -e "------------------------------"
#################
echo -en "Moving on (to Create AttributeBackendMapping for column company in Cost) ? [Hit Return] "
read -r RESP
########### Cost.company
# Find the id of "company" in the "Cost" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes
COST_COMPANY=$(curl --location --request GET "${REST_REQ}" \
                         --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                         --header 'Accept: application/json' | jq '.items[] | select (.name == "company") | "\(.id)" ')
COST_COMPANY=$(echo "${COST_COMPANY}" | tr -d '"')
echo -e "------------------------------"
echo -e "COST.COMPANY is ${COST_COMPANY}"
echo -e "------------------------------"
# Create AttributeBackendMapping for column company
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${COST_ENTITY_ID}/attributes/${COST_COMPANY}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_COST}\",
    \"type\": \"SQL\"
  },
  \"columnName\": \"Company\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column company"
echo -e "Using ${REST_REQ} \nwith\n${PAYLOAD_2}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_COST_COMPANY=$(jq '(.id)' out.txt)
BACKEND_COST_COMPANY=$(echo "${BACKEND_COST_COMPANY}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for cost.company: ${BACKEND_COST_COMPANY}"
echo -e "------------------------------"
#################
echo -en "Done with Cost. [Hit Return] "
read -r RESP
#
#  End of Cost
#
##############################
#
# AR Entity
#
# Will need attributes ar, total_ar
#
echo -en "Moving on (to Create AttributeBackendMapping for column ar) ? [Hit Return] "
read -r RESP
######## AR.ar
# Find the id of "ar" in the "AR" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/attributes
AR_AR_ID=$(curl --location --request GET "${REST_REQ}" \
                    --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                    --header 'Accept: application/json' | jq '.items[] | select (.name == "ar") | "\(.id)" ')
AR_AR_ID=$(echo "${AR_AR_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "AR.ar is ${AR_AR_ID}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column ar
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/attributes/${AR_AR_ID}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_AR}\",
    \"type\": \"SQL\"
  },
  \"columnExpansions\": [ \"AR_\${daysrange}\", \"AR_OVER_\${days_over}\" ]
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column ar"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_AR_ID=$(jq '(.id)' out.txt)
BACKEND_AR_ID=$(echo "${BACKEND_AR_ID}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for AR.ar: ${BACKEND_AR_ID}"
echo -e "------------------------------"
######## AR.total_ar
# Find the id of "total_ar" in the "AR" Entity
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/attributes
TOTAL_AR=$(curl --location --request GET "${REST_REQ}" \
                --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                --header 'Accept: application/json' | jq '.items[] | select (.name == "total_ar") | "\(.id)" ')
TOTAL_AR=$(echo "${TOTAL_AR}" | tr -d '"')
echo -e "------------------------------"
echo -e "AR.total_ar is ${TOTAL_AR}"
echo -e "------------------------------"
#
# Create AttributeBackendMapping for column total_ar
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/bots/${BOT_ID}/entities/${AR_ENTITY_ID}/attributes/${TOTAL_AR}/backendMappings
PAYLOAD_2="{
  \"type\": \"SQL\",
  \"entityMapping\" : {
    \"id\" : \"${BACKEND_ENTITY_AR}\",
    \"type\": \"SQL\"
  },
  \"sqlExpression\": \"AR_01_30 + AR_31_60 + AR_61_90 + AR_91_120 + AR_OVER_120\"
}"
#
echo -e "------------------------------"
echo -e "Create AttributeBackendMapping for column total_ar"
echo -e "Using ${REST_REQ}"
echo -e "------------------------------"
curl --location --request POST "${REST_REQ}" \
     --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
     --header 'Accept: application/json' \
     --header 'Content-Type: application/json' \
     --data-raw "${PAYLOAD_2}" | jq > out.txt
# cat out.txt
BACKEND_TOTAL_AR=$(jq '(.id)' out.txt)
BACKEND_TOTAL_AR=$(echo "${BACKEND_TOTAL_AR}" | tr -d '"')
echo -e "------------------------------"
echo -e "Created Attribute Backend Mapping for AR.total_ar: ${BACKEND_TOTAL_AR}"
echo -e "------------------------------"
echo -e "Done"