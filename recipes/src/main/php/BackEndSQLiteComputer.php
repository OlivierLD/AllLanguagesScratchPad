<?php

class Ingredient {
    public $id;
    public $name;
}

class Recipe {
    public $id;
    public $name;
    public $nb_ing;
}

class BackEndSQLiteComputer {

    private static $db; // SQLite3
    public $stationList = null;  // Array

    public function connectDB(string $location) : void {
        self::$db = new SQLite3($location);
        self::$db->enableExceptions(true); // Error list: https://www.sqlite.org/rescode.html
    }

    public function closeDB() : void {
        self::$db->close();
        self::$db = null;
    }

    public function getDBObject() : ?SQLite3 {
        return self::$db;
    }

    public function buildIngredientList(string $filter) : array {
        if (self::$db == null) {
            throw new Exception("DB Not connected yet.");
        } else {
            $ingredientArray = [];
            $sql = 'SELECT RANK,
                           NAME
                    FROM INGREDIENTS
                    WHERE (UPPER(NAME) LIKE UPPER(\'%' . $filter . '%\'))
                    ORDER BY 2;';

            echo("SQL to execute : [" . $sql . "]<br/>" . PHP_EOL);

			try {
                $results = self::$db->query($sql);
                while ($row = $results->fetchArray()) {
                    // echo ("We have " . $row[0] . "...<br/>" . PHP_EOL);
                    $rank = (float)$row[0];
                    $name = $row[1];

                    $ingredient = new Ingredient();

                    $ingredient->id = $rank;
                    $ingredient->name = $name;

                    array_push($ingredientArray, $ingredient);
                }
			} catch (Throwable $ex) {
				throw $ex;
			}

            return $ingredientArray;
        }
    }

    public function buildRecipeList(string $filter, int $sort_by=2) : array {
        if (self::$db == null) {
            throw new Exception("DB Not connected yet.");
        } else {
            $recipeArray = [];
            $sql = 'SELECT R.RANK,
                           R.NAME,
                           COUNT(IPR.INGREDIENT) AS NB_ING
                    FROM RECIPES R, INGREDIENTS_PER_RECIPE IPR
                    WHERE (UPPER(R.NAME) LIKE UPPER(\'%' . $filter . '%\')) AND (R.RANK = IPR.RECIPE)
                    GROUP BY R.RANK
                    UNION
                    SELECT R.RANK,
                           R.NAME,
                           0 AS NB_ING
                    FROM RECIPES R
                    WHERE R.RANK NOT IN (SELECT RECIPE FROM INGREDIENTS_PER_RECIPE)
                    ORDER BY ' . $sort_by . ';';

            echo("SQL to execute : [" . $sql . "]<br/>" . PHP_EOL);

			try {
                $results = self::$db->query($sql);
                while ($row = $results->fetchArray()) {
                    // echo ("We have " . $row[0] . "...<br/>" . PHP_EOL);
                    $rank = (float)$row[0];
                    $name = $row[1];
                    $nb_ing = (float)$row[2];

                    $recipe = new Recipe();

                    $recipe->id = $rank;
                    $recipe->name = $name;
                    $recipe->nb_ing = $nb_ing;

                    array_push($recipeArray, $recipe);
                }
			} catch (Throwable $ex) {
				throw $ex;
			}

            return $recipeArray;
        }
    }

}