<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Recipes Finder</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <style type="text/css">
		* {
			font-family: Verdana, 'Courier New', Courier, monospace;
		}
    td {
        border: 1px solid black;
        border-radius: 5px;
        padding: 5px;
    }
    </style>
</head>

<body style="background-color: rgba(255, 255, 255, 0.2); background-image: none;">
<!--h1>Recipes DB</h1-->
<h2>Recipes query page...</h2>
<div>
  Entre the ingredients you have (joker is %), comma separated, and you'll get a list of the recipes involving (all of) them.
</div>

<?php
$VERBOSE = false;

ini_set('memory_limit', '-1'); // Required for memory consuming operations...

try {
    set_time_limit(3600); // In seconds. 300: 5 minutes, 3600: one hour
    // phpinfo();
    include __DIR__ . '/autoload.php';

    $VERBOSE = false;

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low.");
    }

    $backend = new BackEndSQLiteComputer();
    if ($VERBOSE) {
      echo("Backend created.<br/>". PHP_EOL);
    }

    // $backend->connectDB("./sql/recipes.db");
    // echo("Connection created.<br/>". PHP_EOL);

    if (isset($_POST['operation'])) {
      $operation = $_POST['operation'];
      if ($operation == 'find-recipes') {
        $ing_list = $_POST['ing-list'];
        echo "<b>Finding Recipes matching ingredients " . $ing_list . " ... </b><br/>" . PHP_EOL;
        $ing_array = explode(",", $ing_list);
        for ($i=0; $i<count($ing_array); $i++) {
          $ing_array[$i] = trim($ing_array[$i]);
        }

        /*
        SELECT DISTINCT R1.RECIPE_ID, R1.RECIPE_NAME
FROM RECIPES_EXTENDED R1
WHERE EXISTS (SELECT R2.INGREDIENT_NAME  FROM RECIPES_EXTENDED R2 WHERE R2.RECIPE_ID = R1.RECIPE_ID AND UPPER(R2.INGREDIENT_NAME) LIKE UPPER('%lieu noir%'))
       AND EXISTS (SELECT R2.INGREDIENT_NAME  FROM RECIPES_EXTENDED R2 WHERE R2.RECIPE_ID = R1.RECIPE_ID AND UPPER(R2.INGREDIENT_NAME) LIKE UPPER('%coco%'))
GROUP BY RECIPE_ID
        */
        $sql = "SELECT DISTINCT R1.RECIPE_ID, R1.RECIPE_NAME FROM RECIPES_EXTENDED R1 WHERE ";
        for ($i=0; $i<count($ing_array); $i++) {
          if ($i > 0) {
            $sql .= " AND ";
          }
          $sql .= "EXISTS (SELECT R2.INGREDIENT_NAME  FROM RECIPES_EXTENDED R2 WHERE R2.RECIPE_ID = R1.RECIPE_ID AND UPPER(R2.INGREDIENT_NAME) LIKE UPPER('%" . $ing_array[$i] . "%'))";
        }
        $sql .= " GROUP BY R1.RECIPE_ID ;";

        echo("SQL to execute : [" . $sql . "]<br/>" . PHP_EOL);

        try {
          $backend->connectDB("./sql/recipes.db");
          if ($VERBOSE) {
            echo("Connection created.<br/>". PHP_EOL);
          }

          $db = $backend->getDBObject();
          $results = $db->query($sql);

          // echo("Found " . count($arrayResults) . " recipe(s) matching your ingredients.<br/>" . PHP_EOL);

          echo("<h2>Recipes</h2>");
          echo "<table>";
          echo "<tr><th>Rank</th><th>Recipe</th><th>Details</th></tr>";
          while ($row = $results->fetchArray()) {
              // echo ("We have " . $row[0] . "...<br/>" . PHP_EOL);
              $rank = (float)$row[0];
              $name = $row[1];
              echo ("<tr><td>" . $rank . "</td><td>" . $name . "</td><td>" .
                      "<form action=\"" . "recipes.php" . "\" method=\"post\">" .
                        "<input type=\"hidden\" name=\"operation\" value=\"details\">" .
                        "<input type=\"hidden\" name=\"rec-id\" value=\"" . $rank . "\">" .
                        "<input type=\"submit\" value=\"Details\">" .
                     "</form>" . "</td></tr>" . PHP_EOL);
          }
          echo "</table>";

          // On ferme !
          $backend->closeDB();
          if ($VERBOSE) {
            echo("Closed DB<br/>".PHP_EOL);
          }
        } catch (Throwable $e) {
          echo "Captured Throwable for connection : " . $e->getMessage() . "<br/>" . PHP_EOL;
        }
        ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="get">
          <!--input type="hidden" name="operation" value="blank"-->
          <table>
            <tr>
              <td colspan="2" style="text-align: center;"><input type="submit" value="Query Again ?"></td>
            </tr>
          </table>
        </form>

        <?php
      }
    } else { // Then display the query form
        ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="post">
          <input type="hidden" name="operation" value="find-recipes">
          <table>
            <tr>
              <td valign="top">Ingredients list, comma separated:</td><td><input type="text" name="ing-list" size="80"></td>
            </tr>
            <tr>
              <td colspan="2" style="text-align: center;"><input type="submit" value="Query"></td>
            </tr>
          </table>
        </form>
        <?php
    }
    // echo("Loop Completed.<br/>". PHP_EOL);
} catch (Throwable $plaf) {
    echo "[Captured Throwable (big) for recipes.ingredients.php : " . $plaf . "] " . PHP_EOL;
}
?>
</body>
</html>