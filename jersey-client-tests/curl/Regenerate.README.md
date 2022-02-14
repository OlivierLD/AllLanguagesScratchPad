# Regeneration from OMRL to SQL

This utility provides the possibility to generate and/or execute a SQL statement,
based on
- an OMRL Mapping Schema
- an OMRL Query

It relies on the output of the generation of the OMRL queries from the Spider dataset
```
$ bots/tools/sqlparser> mvn clean install
$ bots/tools/sqlparser> java -jar target/sqlparser-1.0-SNAPSHOT.jar --spider ../../../spider_sample_data/train_spider.json --output ORML_v2
```

<!-- The code is in `oracle.cloud.bots.component.omrl.utils.StandaloneOMRL2SQLTester`, it uses the exact same utilities as the 
ODA System Component `oracle.cloud.bots.component.omrl.OMRLtoSQLComponent`. -->  

This can be executed from a shell script named `regenerate.sh`, that can take several CLI parameters.

### How to build
From your laptop, or from your ODA instance:
- Checkout the current branch
- From the folder this document you are reading is, run
```
$ ../../../gradlew shadowJar
```
This will generate a `build/libs/inbuilt-components-19.1.3-100-all.jar` that is referred to from the
`regenerate.sh` script.
 
### How to run
```
$ ./regenerate.sh --spider-output:../../OMRL_v3_out.json > good.txt 2> bad.txt
```
The errors (Exceptions) would go in `bad.txt`.  
The rest in `good.txt`

> _Note_: The `omrl` queries contained in the `sqlparser` result
> can be run with the `omrl2sql` utility.
> ```
> $ ./omrl2sql.sh --execute-query:false --query-file:omrl.q.04.json --schema-name:tracking_software_problems --use-prepare-stmt:false
> SQL: (SELECT Problems.problem_id FROM Problems JOIN Staff ON Staff.staff_id = Problems.reported_by_staff_id WHERE (Staff.staff_first_name = 'Dameon') AND (Staff.staff_last_name = 'Frami')) UNION (SELECT Problems.problem_id FROM Problems JOIN Staff ON Staff.staff_id = Problems.reported_by_staff_id WHERE (Staff.staff_first_name = 'Jolie') AND (Staff.staff_last_name = 'Weber'))  
> ```
> In the statement above, `omrl.q.04.json` contains the OMRL query.

### What we can see
Some errors come from the way the schemas are defined.  
Some others from the way the original queries are defined.  
Some from the resulting OMRL queries.

- `[ "#COUNT", [ "*" ] ]`  
The `*` is missing.
```
{
  "sql" : "SELECT T2.name ,  count(*) FROM race AS T1 JOIN track AS T2 ON T1.track_id  =  T2.track_id GROUP BY T1.track_id",
  "omrl" : {
    "keyWord" : "",
    "select" : [ [ "track", "name" ], [ "#COUNT", [ "" ] ] ],
    "from" : "race",
    "where" : [ ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ [ "TRACK_ID" ] ]
  }
}
```

- `[ "problem_log_assigned_to_staff_back_link", "" ]`  
Missing column/attribute name
```
{
  "sql" : "SELECT DISTINCT staff_first_name ,  staff_last_name FROM staff AS T1 JOIN problem_log AS T2 ON T1.staff_id = T2.assigned_to_staff_id WHERE T2.problem_id = 1",
  "schemaName" : "tracking_software_problems",
  "omrl" : {
    "keyWord" : "DISTINCT",
    "select" : [ [ "staff_first_name" ], [ "staff_last_name" ] ],
    "from" : "staff",
    "where" : [ "=", [ "problem_log_assigned_to_staff_back_link", "" ], "1" ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  }
}
```

- Bad column name, empty select clause (`product_id` not in `Problems`)
```
{
  "sql" : "SELECT product_id FROM problems AS T1 JOIN staff AS T2 ON T1.reported_by_staff_id = T2.staff_id WHERE T2.staff_first_name = \"Dameon\" AND T2.staff_last_name = \"Frami\" UNION SELECT product_id FROM problems AS T1 JOIN staff AS T2 ON T1.reported_by_staff_id = T2.staff_id WHERE T2.staff_first_name = \"Jolie\" AND T2.staff_last_name = \"Weber\"",
  "schemaName" : "tracking_software_problems",
  "omrl" : [ "UNION", {
    "keyWord" : "",
    "select" : [ [ "" ] ],
    "from" : "problems",
    "where" : [ "AND", [ "=", [ "reported_by_staff", "staff_first_name" ], "Dameon" ], [ "=", [ "reported_by_staff", "staff_last_name" ], "Frami" ] ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  }, {
    "keyWord" : "",
    "select" : [ [ "" ] ],
    "from" : "problems",
    "where" : [ "AND", [ "=", [ "reported_by_staff", "staff_first_name" ], "Jolie" ], [ "=", [ "reported_by_staff", "staff_last_name" ], "Weber" ] ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  } ]
}
```

- `"having" : [ ">", [ "AVG", [ "bikes_available" ] ], "10" ], => "having" : [ ">", [ "#AVG", [ "bikes_available" ] ], "10" ],`  
-... All functions in `HAVING` seem to miss the preceding '#'.
- `"10" ],` => `10 ],`. Do not put quotes around the numbers.
- Empty `groupBy` in the second SELECT, probably because of the schema.
```
{
  "sql" : "SELECT id FROM station WHERE city  =  \"San Francisco\" INTERSECT SELECT station_id FROM status GROUP BY station_id HAVING avg(bikes_available)  >  10",
  "schemaName" : "bike_1",
  "omrl" : [ "INTERSECT", {
    "keyWord" : "",
    "select" : [ [ "id" ] ],
    "from" : "station",
    "where" : [ "=", [ "city" ], "San Francisco" ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  }, {
    "keyWord" : "",
    "select" : [ [ "station" ] ],
    "from" : "status",
    "where" : [ ],
    "orderBy" : [ ],
    "having" : [ ">", [ "AVG", [ "bikes_available" ] ], "10" ],
    "groupBy" : [ [ "" ] ]
  } ]
}
```

- For the `station_id` above, schema is
```
[
  {
      "name": "station",
      "foreignKey": "station_id",
      "targetForeignKey": null,
      "uniqueValues": false
  },
```
- no `columnName`? (and `foreignKey` should be an array)



- In the `BETWEEN` clause below: Where is the 15 ?
```
{
  "sql" : "SELECT avg(num_employees) FROM department WHERE ranking BETWEEN 10 AND 15",
  "schemaName" : "department_management",
  "omrl" : {
    "keyWord" : "",
    "select" : [ [ "#AVG", [ "num_employees" ] ] ],
    "from" : "department",
    "where" : [ "BETWEEN", [ "ranking" ], "10" ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  }
}
```

- Where clause!!
```
{
  "sql" : "SELECT count(*) FROM courses AS T1 JOIN student_course_attendance AS T2 ON T1.course_id = T2.course_id WHERE T2.student_id = 171",
  "schemaName" : "student_assessment",
  "omrl" : {
    "keyWord" : "",
    "select" : [ [ "#COUNT", [ "" ] ] ],
    "from" : "courses",
    "where" : [ "=", [ null, "" ], "171" ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  }
}
```

- Unwanted back quotes 
```
{
  "sql" : "SELECT LName FROM Student WHERE age  =  (SELECT min(age) FROM Student)",
  "schemaName" : "allergy_1",
  "omrl" : {
    "keyWord" : "",
    "select" : [ [ "last_name" ] ],
    "from" : "student",
    "where" : [ "=", [ "age" ], "SELECT MIN(`AGE`)\nFROM `STUDENT`" ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  }
}
```

- `"'PHL'"` -> `"PHL"`, `"20"` -> `20`, `BETWEEN`, `AND` missing
```
{
  "sql" : "SELECT fname FROM student WHERE city_code  =  'PHL' AND age BETWEEN 20 AND 25",
  "schemaName" : "dorm_1",
  "omrl" : {
    "keyWord" : "",
    "select" : [ [ "first_name" ] ],
    "from" : "student",
    "where" : [ "AND", [ "=", [ "city_code" ], "'PHL'" ], [ "BETWEEN", [ "age" ], "20" ] ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  }
}
```

- Select `*`. Do we allow that?
```
{
  "sql" : "SELECT * FROM employees WHERE salary BETWEEN 8000 AND 12000 AND commission_pct != \"null\" OR  department_id != 40",
  "schemaName" : "hr_1",
  "omrl" : {
    "keyWord" : "",
    "select" : [ [ "" ] ],
    "from" : "employees",
    "where" : [ "OR", [ "AND", [ "BETWEEN", [ "salary" ], "8000" ], [ "<>", [ "commission_pct" ], "null" ] ], [ "<>", [ "" ], "40" ] ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  }
}
```

- `IN SELECT`, `NOT IN SELECT`. Shouldn't the `SELECT` be represented as the ones in `UNION`, `INTERSECT`, etc?
- What is this `ASYMETRIC` in the `SELECT` in the `where` clause? 
```
{
  "sql" : "SELECT * FROM employees WHERE department_id NOT IN (SELECT department_id FROM departments WHERE manager_id BETWEEN 100 AND 200)",
  "schemaName" : "hr_1",
  "omrl" : {
    "keyWord" : "",
    "select" : [ [ "" ] ],
    "from" : "employees",
    "where" : [ "NOT IN", [ "" ], "SELECT `DEPARTMENT_ID`\nFROM `DEPARTMENTS`\nWHERE `MANAGER_ID` BETWEEN ASYMMETRIC 100 AND 200" ],
    "orderBy" : [ ],
    "having" : [ ],
    "groupBy" : [ ]
  }
}
```

---
