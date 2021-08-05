See <https://confluence.oraclecorp.com/confluence/pages/viewpage.action?spaceKey=IBS&title=Learnings+from+OMRL+v0+and+Next+Steps>

From the section "Here would be an example from Mark's OMRL proposal."

For the OMRL schema
```json
{
  "tables": [{
    "table_name": "Airports",
    "columns": [{
      "column_name": "City"
    },{
        "column_name": "AirportCode"
      }
    ]
  },
    {
      "table_name": "Flights",
      "columns": [{
        "column_name": "FlightNumber"
      }],
      "links": [{
        "link_name": "SourceAirport",
        "linked_table": "Airports"
      },
        {
          "link_name": "DestAirport",
          "linked_table": "Airports"
        }
      ]
    }
  ]
}
```
and the OMRL query represented like
```json
{
  "tables": [
    {
      "table_name": "Airports",
      "columns": [
        {
          "column_name": "City"
        },
        {
          "column_name": "AirportCode"
        }
      ]
    },
    {
      "table_name": "Flights",
      "columns": [
        {
          "column_name": "FlightNumber"
        }
      ],
      "links": [
        {
          "link_name": "SourceAirport",
          "linked_table": "Airports"
        },
        {
          "link_name": "DestAirport",
          "linked_table": "Airports"
        }
      ]
    }
  ]
}
```
The SQL Query would be 
```sql
SQL> SELECT COUNT( * ) FROM flights JOIN airports ON flights.destairport = airports.airportcode WHERE airports.city = ’Aberdeen’;
```

> Questions:
> - Where does the JOIN clause come from?
> - Where to get the column type from?

