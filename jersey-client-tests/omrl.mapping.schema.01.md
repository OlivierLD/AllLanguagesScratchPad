# Bag Entities representation

Entities

Name 
```
race
```
Description
```
@database(name="race_track"), @table(name=race)
```
Configuration
```
Composite Bag
```

Bag Items

| Name     | Type   | Entity Name | Sequence Number | Description                                                      |
|:---------|:-------|:------------|:----------------|:-----------------------------------------------------------------|
| race_id  | Entity | NUMBER      | 1               | @column(name=Race_ID),@primary_key(name=Race_ID, entity=race)    |
| name     | String |             | 2               | @column(name=Name)                                               |
| class    | String |             | 3               | @column(name=Class)                                              |
| date     | String |             | 4               | @column(name=Race_Date)                                          |
| track_id | Entity | NUMBER      | 5               | @column(name=Track_ID),@foreign_key(name=Track_ID, entity=track) |

---

Name
```
track
```
Description
```
@database(name="race_track"),@table(name=track)
```
Configuration
```
Composite Bag
```

Bag Items

| Name        | Type   | Entity Name | Sequence Number | Description                                                                                          |
|:------------|:-------|:------------|:----------------|:-----------------------------------------------------------------------------------------------------|
| track_id    | Entity | NUMBER      | 1               | @column(name=Track_ID),@primary_key(name=Track_ID, entity=track),@foreign_key(name=Track_ID,entity=race) |
| name        | String |             | 2               | @column(name=Name)                                                                                   |
| location    | String |             | 3               | @column(name=Location)                                                                               |
| seating     | Entity | NUMBER      | 4               | @column(name=Seating)                                                                                |
| year_opened | Entity | NUMBER      | 5               | @column(name=Year_Opened)                                                                            |

----
