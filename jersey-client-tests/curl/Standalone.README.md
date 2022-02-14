# Standalone OMRL 2 SQL Utility
This utility provides the possibility to generate and/or execute a SQL statement,
based on
- an OMRL Mapping Schema
- an OMRL Query

The code is in `oracle.cloud.bots.component.omrl.utils.StandaloneOMRL2SQL`, it uses the exact same utilities as the 
ODA System Component `oracle.cloud.bots.component.omrl.OMRLtoSQLComponent`.  
This can be executed from a shell script named `omrl2sql.sh`, that can take several CLI parameters.
```
$ ./omrl2sql.sh --help
---------------------------------------------------------------------
This is a standalone utility to :
 - Convert an OMRL Query into a SQL Query
 - Execute it (optional)
 - Get the returned ResultSet (if executed)
---------------------------------------------------------------------
 See below the Command Line parameters. They all have a default value.
 See the code in oracle.cloud.bots.component.omrl.utils.StandaloneOMRL2SQL for details.
---------------------------------------------------------------------
CLI parameters:
--help, you're looking at it.
--verbose:, like in --verbose:true
--execute-query:, like in --execute-query:true
--use-prepare-stmt:, like in --use-prepare-stmt:true
--mapping-schema:, like in --mapping-schema:path/to/schema.json
--mapping-sql-schema:, like in --mapping-sql-schema:path/to/sql-schema.json
--list-schema-names:, like in --list-schema-names:true
--schema-name:, like in --schema-name:race_track
--query-index:, like in --query-index:[0..10]
--query-file:, like in --query-file:path/to/omrl-query.json

--db-host:, like in --db-host:100.111.136.104
--db-port:, like in --db-port:1521
--db-service-name:, like in --db-service-name:BOTS.localdomain
--db-username:, like in --db-username:scott
--db-password:, like in --db-password:tiger

$
```

### How to build
From your laptop, or from your ODA instance:
- Checkout the current branch
- From the folder this document you are reading is, run
```
$ ../../../gradlew shadowJar
```
This will generate a `build/libs/inbuilt-components-19.1.3-100-all.jar` that is referred to from the
`omrl2sql.sh` script.
 
### How to run
There are default values for all of those parameters (look into the code for details).

Several documents are pre-baked, for clarity. They are in this [resource](./src/main/resources) folder. 
You have example of [OMRL Mapping Schemas](./src/main/resources/omrl.mapping.schema.01.json), as well as
[OMRL queries](./src/main/resources/omrl.race_track.query.02.json).

The `--query-index:` parameter refers to one of the 11 available queries (`[0..10]`).  
The `--schema-name:` refers to the schema name, as it is defined in OMRL Mapping Schemas, as `race_track` in this:
```
{
  "race_track" : {
    "entities" : [ {
      "name" : "race",
      "attributes" : [ {
        "name" : "race id",
        "type" : "number",
        "@column" : "Race_ID"
      }, {
. . .
```
This `--schema-name:` is redundant with the DB parameters (username, password, url, etc),
this will be addressed later.

When the query is executed, the result set is returned in a JSON format, and can be formatted as such,
with a utility like `jq`.

Raw format:
```
$ ./omrl2sql.sh --query-index:2
[{"name":"Gainsco Grand Prix of Miami","race_date":"March 29","track":{"name":"Chicagoland Speedway"}},{"name":"Mexico City 250","race_date":"April 19","track":{"name":"Chicagoland Speedway"}}]
$
```

Formatted:
```
$  ./omrl2sql.sh --query-index:2 | jq
  [
    {
      "name": "Gainsco Grand Prix of Miami",
      "race_date": "March 29",
      "track": {
        "name": "Chicagoland Speedway"
      }
    },
    {
      "name": "Mexico City 250",
      "race_date": "April 19",
      "track": {
        "name": "Chicagoland Speedway"
      }
    }
  ]
```

Here is an example showing how to use your own schema and query, to finally display the SQL query to execute:
```
$ ./omrl2sql.sh \
         --mapping-schema:./src/main/resources/OMRL_base_schema.json \
         --mapping-sql-schema:./src/main/resources/OMRL_sql_schema.json \
         --schema-name:race_track \
         --query-file:./src/main/resources/omrl.race_track.query.03.json \
         --execute-query:false
  SQL: SELECT race.name, race.race_date, track.name FROM race JOIN track ON track.Track_ID = race.Track_ID WHERE track.name = 'Chicagoland Speedway' 
```

### Example: On the TetraTech Schema
```
$ ./omrl2sql.sh --db-host:100.111.137.196 \
                --db-port:1521 \
                --db-service-name:BOTS.localdomain \
                --db-username:tetratech \
                --db-password:tetratechtetratechtetratech \
                --mapping-schema:./src/main/resources/OMRL_base_schema_4t.json \
                --mapping-sql-schema:./src/main/resources/OMRL_sql_schema_4t.json \
                --schema-name:tetra_tech \
                --execute-query:true \
                --query-file:src/main/resources/omrl.tetra_tech.query.02.json | jq

```
This would produce something like this:
```
[
  {
    "cost": {
      "cost": 524388
    },
    "company": "111 AAA"
  },
  {
    "cost": {
      "cost": 97926
    },
    "company": "111 AAA"
  },
  {
    "cost": {
      "cost": 20450
    },
    "company": "111 AAA"
  },
  {
. . .
```

---
