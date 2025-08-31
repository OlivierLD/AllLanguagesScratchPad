-- for sqlite3
-- use:
-- sqlite3 sql/recipes.db < sql/create.tables.sql
-- or
-- sqlite3 db/recipes.db
-- sqlite> .read sql/create.tables.sql
--------------------------------------------
--
-- drop tables first
--
drop table equilibriums;
drop table nodefactors;
drop table speedconstituents;
drop table stationdata;
drop table stations;
drop table coeffdefs;
--
select 'All tables dropped';

--
-- create tables
--
create table coeffdefs (
  rank integer not null primary key,
  name varchar(64) not null,
  constraint uk_name_coeffedefs unique (name)
);