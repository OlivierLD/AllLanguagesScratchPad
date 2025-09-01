--
-- Just an example
--
insert into ingredients (name) values ('Pak choï');
insert into ingredients (name) values ('Oignon nouveau');
insert into ingredients (name) values ('Oignon');
insert into ingredients (name) values ('Gousse d''ail');
insert into ingredients (name) values ('Portobello');
insert into ingredients (name) values ('Cacahuètes non salées');
insert into ingredients (name) values ('Piment');
insert into ingredients (name) values ('Nouilles de blé');
insert into ingredients (name) values ('Sauce soja');
insert into ingredients (name) values ('Beurre de cacahuètes');
insert into ingredients (name) values ('Huile de sésame');

insert into ingredients (name) values ('Riz');
insert into ingredients (name) values ('Carotte');
insert into ingredients (name) values ('Citron');
insert into ingredients (name) values ('Filet de lieu noir');
insert into ingredients (name) values ('Curry jaune');
insert into ingredients (name) values ('Épinards');
insert into ingredients (name) values ('Lait de coco');

insert into ingredients (name) values ('Poivron');
insert into ingredients (name) values ('Paprika fumé en poudre');
insert into ingredients (name) values ('Persil');
insert into ingredients (name) values ('Thym séché');


insert into recipes (name) values ('Wok de nouilles au portobello & pak choï');
insert into recipes (name) values ('Lieu, épinards & coco façon curry');
insert into recipes (name) values ('Lieu à la jamaïcaine, persil & riz pilaf');

select * from ingredients;
select * from recipes;

insert into ingredients_per_recipe (recipe, ingredient) values (1, 1);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 2);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 3);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 4);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 5);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 6);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 7);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 8);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 9);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 10);
insert into ingredients_per_recipe (recipe, ingredient) values (1, 11);

insert into ingredients_per_recipe (recipe, ingredient) values (
    (select rank from recipes where upper(name) like upper('%pinards & coco%') limit 1),
    (select rank from ingredients where upper(name) like upper('%gousse%') limit 1)
);

insert into ingredients_per_recipe (recipe, ingredient) values (2, 12);

select r.name, i.name
from ingredients_per_recipe t, recipes r, ingredients i
where t.recipe = r.rank
  and t.ingredient = i.rank
order by 1, 2;

select recipe_name, count(*) from recipes_extended group by recipe_name;