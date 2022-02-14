# TetraTech System Component at work

### Get started
- From the ODA Web UI
    - Import the skill `OMRL to SQL (with BagEntity)`
    - Clone it into `OMRL2SQL4TECH`
    - If needed, drop the existing Bag Entities (`race`, and `track`).
    - Note the BotID (like `039740B0-5772-4E87-9986-98283AF0FBBF`)
- From a Terminal
    - Modify `curl.4tech.sh` to match your ODA instance IP address
    - Run the script `curl.4tech.sh`, with the BotID from above
- From the Web UI, in the bot `OMRL2SQL4TECH`
    - Edit the flow
    - Look for the state `omrlToSQL`
    - Modify it like this (warning, it's case-sensitive):
    ```yaml
  omrlToSQL:
        component: System.OMRLtoSQL
        properties:
          db-id: ${ enteredDBID }
          DB-user-name: "tetratech"
          DB-password: "tetratechtetratechtetratech"
          db-url: "jdbc:oracle:thin:@//100.111.137.196:1521/BOTS.localdomain"
          omrl-schema-param: { name: "TetraTech", schemaEntities: "Cost, AR" }
          omrl-json: ${ enteredOMRLJson }
          query-output: outcome
          exclude-from-proxy-list: false
          execute-query: true
          verbose: false
        transitions: 
          actions:
            success: "omrlOk"
            failure: "problems_00"
          next: "done"
   ```        
    - Run it
        - When prompted for the DB_ID, enter `tetra_tech`
        - When prompted for a Query, try
        ```json
        {
          "select": [
            [ "ar" ]
          ],
          "from": "AR",
          "where": [
            "AND",
                [ "=", [ "daysrange" ], "01_30" ],
                [ "=", [ "days_over" ], "120" ]
          ]
        }
        ```     
        or
        ```json
        {
          "select": [
            [ "cost" ]
          ],
          "from": "Cost",
          "where": [
            "AND",
                [ "=", [ "period" ], "YTD" ],
                [ "=", [ "type" ], "LBR" ]
          ],
          "limit": 10
        }
        ```
---
 