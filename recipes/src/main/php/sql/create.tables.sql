-- See https://www.sqlitetutorial.net/
--
-- for sqlite3
-- use:
-- sqlite3 sql/recipes.db < sql/create.tables.sql
-- or
-- sqlite3 db/recipes.db
-- sqlite> .read sql/create.tables.sql
--
-- To see the table structure:
-- .schema recipes
-- or
-- SELECT sql FROM sqlite_schema WHERE name = 'recipes';
--
-- view all tables:
-- .tables
--------------------------------------------
--
-- drop tables first
--
drop table ingredients_per_recipe;
drop table recipes;
drop table ingredients;
--
select 'All tables dropped';

--
-- create tables
--
create table ingredients (
  rank integer not null primary key autoincrement,
  name varchar(64) not null,
  constraint uk_name_ingredients unique (name)
);

create table recipes (
  rank integer not null primary key autoincrement,
  name varchar(64) not null,
  constraint uk_name_recipes unique (name)
);

create table ingredients_per_recipe (
  recipe integer not null,
  ingredient integer not null,
  constraint pk_ing_per_rec primary key (recipe, ingredient),
  constraint ing_per_rec_fk_ingredients foreign key (ingredient) references ingredients(rank) on delete cascade,
  constraint ing_per_rec_fk_recipes foreign key (recipe) references recipes(rank) on delete cascade
);

select 'All tables created';

drop view recipes_extended;

create view recipes_extended as
select r.rank as recipe_id, r.name as recipe_name, i.rank as ingredient_id, i.name as ingredient_name
from ingredients_per_recipe t, recipes r, ingredients i
where t.recipe = r.rank
  and t.ingredient = i.rank;