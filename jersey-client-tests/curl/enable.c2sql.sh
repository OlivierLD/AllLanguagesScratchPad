#!/bin/bash
#
# Enable C2SQL.
# Do once on a new Instance.
#
ODA_INSTANCE=100.111.136.104
BOTS_TENANT_ID=odaserviceinstance00
#
REST_REQ=http://${ODA_INSTANCE}:8181/v1/storage/bots-container/oda_la_whitelist/ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda/config.json
#
PAYLOAD="{ \"whitelistedFeatures\": [\"CONV2SQL\"] }"
#
curl --location --request PUT "${REST_REQ}" \
                --header "Bots-TenantId: ${BOTS_TENANT_ID}" \
                --header 'Content-Type: application/json' \
                --data-raw "${PAYLOAD}" > out.txt
#
cat out.txt | jq
#
