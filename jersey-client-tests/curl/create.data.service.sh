#!/bin/bash
#
# Create dataService (race_track)
# Do once on a new Instance.
#
ODA_INSTANCE=100.111.136.104
BOTS_TENANT_ID=odaserviceinstance00
#
REST_REQ=http://${ODA_INSTANCE}:9990/management-api/v1/dataServices
#
# Modify name, hostname, username, password, etc at will in the payload.
#
curl --location --request POST "${REST_REQ}" \
     --header 'Bots-TenantId: odaserviceinstance00' \
     --header 'Content-Type: application/json' \
     --data-raw '{
	"name": "race_track",
	"description": "Race Track DB",
	"databaseType": "Oracle",
	"authenticationType": "Default",
	"connectionType": "Basic",
	"role": "default",
	"username": "races",
	"password": "racesracesracesraces",
	"hostname": "100.111.136.104",
	"port": 1521,
	"sid": "",
	"serviceName": "BOTS.localdomain"
}' > out.txt
#
cat out.txt | jq
#
