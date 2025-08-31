--
-- Just an example
--
insert into ingredients (name) values ('Pak choï');
insert into ingredients (name) values ('Oignon nouveau');

insert into recipes (name) values ('Wok de nouilles au portobello & pak choï');

select * from ingredients;
select * from recipes;

insert into ingredients_per_recipe (recipe, ingredient) values (1, 1);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 2);

select r.name, i.name
from ingredients_per_recipe t, recipes r, ingredients i
where t.recipe = r.rank
  and t.ingredient = i.rank
order by 1, 2;