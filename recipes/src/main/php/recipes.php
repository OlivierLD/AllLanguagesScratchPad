<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Recipes</title>
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
<h2>Recipes, and others...</h2>

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
      if ($operation == 'query') { // Then do the query
        try {
          $rec_name = $_POST['rec-name'];

          $backend->connectDB("./sql/recipes.db");
          if ($VERBOSE) {
            echo("Connection created.<br/>". PHP_EOL);
          }

          $recipes = $backend->buildRecipeList($rec_name);
          echo("Returned " . count($recipes) . " row(s)<br/>");

          echo("<h2>Recipes</h2>");

          ?> <!-- To Recipe creation form -->

          <form action="<?php echo(basename(__FILE__)); ?>" method="post">
            <input type="hidden" name="operation" value="create">
            <input type="submit" value="Create New Recipe">
          </form>

          <?php

          echo "<table>";
          echo "<tr><th>Rank</th><th>Recipe</th><th>Nb Ingredients</th></tr>";

          for ($i=0; $i<count($recipes); $i++) {
            $recipe = $recipes[$i];
            echo(
              "<tr><td>" . urldecode($recipe->id) . "</td>" .
                  "<td>" . urldecode($recipe->name) . "</td>" .
                  "<td>" . $recipe->nb_ing . "</td>" .
                  "<td>" .
                     "<form action=\"" . basename(__FILE__) . "\" method=\"post\">" .
                        "<input type=\"hidden\" name=\"operation\" value=\"edit\">" .
                        "<input type=\"hidden\" name=\"rec-id\" value=\"" . $recipe->id . "\">" .
                        "<input type=\"hidden\" name=\"rec-name\" value=\"" . urldecode($recipe->name) . "\">" .
                        "<input type=\"submit\" value=\"Edit\">" .
                     "</form>" .
                  "</td>" .
                  "<td>" .
                     "<form action=\"" . basename(__FILE__) . "\" method=\"post\">" .
                        "<input type=\"hidden\" name=\"operation\" value=\"details\">" .
                        "<input type=\"hidden\" name=\"rec-id\" value=\"" . $recipe->id . "\">" .
                        "<input type=\"submit\" value=\"Details\">" .
                     "</form>" .
                  "</td>" .
                  "<td>" .
                     "<form action=\"" . basename(__FILE__) . "\" method=\"post\">" .
                        "<input type=\"hidden\" name=\"operation\" value=\"delete\">" .
                        "<input type=\"hidden\" name=\"rec-id\" value=\"" . $recipe->id . "\">" .
                        "<input type=\"submit\" value=\"Delete\">" .
                     "</form>" .
                  "</td>" .
              "</tr>\n"
            );
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
        echo("<hr/>" . PHP_EOL);
        // echo("Again ? Click <a href='#'>Here</a>.");
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
      } else if ($operation == 'delete') {
        $rec_id = $_POST['rec-id'];
        if ($VERBOSE) {
          echo "Will delete Recipe " . $rec_id . " from RECIPES... <br/>" . PHP_EOL;
        }
        try {
            $backend->connectDB("./sql/recipes.db");
            if ($VERBOSE) {
              echo("Connection created.<br/>". PHP_EOL);
            }
            $db = $backend->getDBObject();

            $sql = 'DELETE FROM RECIPES WHERE (RANK = ' . ($rec_id) . ')';
            echo('Performing statement <code>' . $sql . '</code><br/>');

            if (true) { // Do perform ?
              try {
                $db->exec($sql);
                echo "OK. Operation performed successfully<br/>" . PHP_EOL;
              } catch (Throwable $e) {
                echo "Captured Throwable for exec() : " . $e->getMessage() . "<br/>" . PHP_EOL;
              }
            } else {
              echo "Stby<br/>" . PHP_EOL;
            }
            // On ferme !
            $backend->closeDB();
            if ($VERBOSE) {
              echo("Closed DB<br/>".PHP_EOL);
            }
          ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="get">
          <input type="submit" value="Query Form">
        </form>
          <?php

        } catch (Throwable $e) {
          echo "Captured Throwable for connection : " . $e->getMessage() . "<br/>" . PHP_EOL;
        }
      } else if ($operation == 'edit') {
        $rec_id = $_POST['rec-id'];
        $rec_name = $_POST['rec-name'];
        echo "Edit Recipe [" . $rec_name . "]... <br/>" . PHP_EOL;
        ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="post">
          <input type="hidden" name="operation" value="update">
          <input type="hidden" name="rec-id" value="<?php echo($rec_id); ?>">
          <table>
            <tr>
              <td valign="top">Recipe :</td><td><input type="text" name="rec-name" size="40" value="<?php echo($rec_name); ?>"></td>
            </tr>
            <tr>
              <td colspan="2" style="text-align: center;"><input type="submit" value="Update"></td>
            </tr>
          </table>
        </form>

        <?php
      } else if ($operation == 'details') {
        echo "Details... <br/>" . PHP_EOL;
        $rec_id = $_POST['rec-id'];
        $ing_filter = $_POST['ing-filter'] ?? '';

        // All ingredients for recipe $rec_id.
        // select * from recipes_extended where recipe_id = :rec_id order by ingredient_name;
        // See https://www.php.net/manual/en/sqlite3.prepare.php

        try {
            $backend->connectDB("./sql/recipes.db");
            if ($VERBOSE) {
              echo("Connection created.<br/>". PHP_EOL);
            }
            $db = $backend->getDBObject();

            $ingList = $backend->buildIngredientList($ing_filter);

            // Get the recipe name too
            $sql = 'SELECT NAME FROM RECIPES WHERE (RANK = ' . ($rec_id) . ')';
            echo('Performing statement <code>' . $sql . '</code><br/>');
            $results = $db->query($sql);
            $rec_name = $results->fetchArray()[0]; // Assume one and only one row...
            echo "<h2>Details for Recipe \"" . urldecode($rec_name) . "\"</h2>";

            $sql = 'SELECT * FROM RECIPES_EXTENDED WHERE RECIPE_ID = :REC_ID ORDER BY INGREDIENT_NAME;';
            echo('Performing statement <code>' . $sql . '</code><br/>');
            $stmt = $db->prepare($sql);
            $stmt->bindValue(':REC_ID', $rec_id, SQLITE3_INTEGER);

            if (true) { // Do perform ?
              try {
                $result = $stmt->execute();
                ?>
                <table>
                  <tr><th>Ingredient</th><th>-</th></tr>
                <?php
                $nbRec = 0;
                while ($row = $result->fetchArray()) {
                    $nbRec += 1;
                    // echo("Row : " . (float)$row[0] . ", " . $row[1] . ", " . (float)$row[2] . ", " . $row[3] . "<br/>");
                    echo(
                      "<tr>" .
                        "<td>" . urldecode($row[3]) . "</td>" .
                        "<td>" .
                          "<form action=\"" . basename(__FILE__) . "\" method=\"post\">" .
                             "<input type=\"hidden\" name=\"operation\" value=\"delete-ingredient\">" .
                             "<input type=\"hidden\" name=\"rec-id\" value=\"" . $rec_id . "\">" .
                             "<input type=\"hidden\" name=\"ing-id\" value=\"" . $row[2] . "\">" .
                             "<input type=\"submit\" value=\"Delete\">" .
                          "</form>" .
                        "</td>" .
                      "</tr>" . PHP_EOL);
                }
                if ($nbRec == 0) {
                  echo ("<tr><td colspan='2'>Rien pour le moment</td></tr>" . PHP_EOL);
                }
                ?>
                </table>
                <?php
                echo "OK. Operation performed successfully<br/>" . PHP_EOL;
                echo "<hr/>" . PHP_EOL;
              } catch (Throwable $e) {
                echo "Captured Throwable for exec() : " . $e->getMessage() . "<br/>" . PHP_EOL;
              }
            } else {
              echo "Stby<br/>" . PHP_EOL;
            }
            // On ferme !
            $backend->closeDB();
            if ($VERBOSE) {
              echo("Closed DB<br/>".PHP_EOL);
            }
        ?>
        <!-- Filter on ingredients, to shorten the list -->
        <form action="<?php echo(basename(__FILE__)); ?>" method="post">
          <input type="hidden" name="operation" value="details">
          <input type="hidden" name="rec-id" value="<?php echo($rec_id); ?>">
          <input type="text" name="ing-filter" value="<?php echo($ing_filter); ?>" placeholder="Filter on Ingredients">
          <input type="submit" value="Filter on Ingredients List">
        </form>

        <form action="<?php echo(basename(__FILE__)); ?>" method="post">
          <input type="hidden" name="operation" value="add-ingredient">
          <input type="hidden" name="rec-id" value="<?php echo($rec_id); ?>">
          <table>
            <tr>
              <td valign="top">Ingredient :</td>
              <td>
                <select name="ing-id">
                  <?php
                    foreach($ingList as $ing) {
                      echo('<option value="' . $ing->id . '">' . urldecode($ing->name) . '</option>');
                    }
                  ?>
                </select>
              </td>
            </tr>
            <tr>
              <td colspan="2" style="text-align: center;"><input type="submit" value="Add Ingredient"></td>
            </tr>
          </table>
        </form>
        <hr/>
        <form action="<?php echo(basename(__FILE__)); ?>" method="get">
          <table>
            <tr>
              <td colspan="2" style="text-align: center;"><input type="submit" value="Back to recipes query"></td>
            </tr>
          </table>
        </form>


        <?php
        } catch (Throwable $e) {
          echo "Captured Throwable for connection : " . $e->getMessage() . "<br/>" . PHP_EOL;
        }
      } else if ($operation == 'add-ingredient') {
        // add-ingredient to a recipe
        $rec_id = $_POST['rec-id'];
        $ing_id = $_POST['ing-id'];

        // Insert ingredient into recipe
        echo "Will add Ingredient " . $ing_id . " to Recipe " . $rec_id . "... <br/>" . PHP_EOL;

        $sql = 'INSERT INTO INGREDIENTS_PER_RECIPE (RECIPE, INGREDIENT) VALUES (' . ($rec_id) . ', ' . ($ing_id) . ')';
        echo('Performing statement <code>' . $sql . '</code><br/>');
        if (true) { // Do perform ?
          try {
            $backend->connectDB("./sql/recipes.db");
            if ($VERBOSE) {
              echo("Connection created.<br/>". PHP_EOL);
            }
            $db = $backend->getDBObject();

            $db->exec($sql);
            echo "OK. Operation performed successfully<br/>" . PHP_EOL;
            // On ferme !
            $backend->closeDB();
            if ($VERBOSE) {
              echo("Closed DB<br/>" . PHP_EOL);
            }
          } catch (Throwable $e) {
            echo "Captured Throwable for exec() : " . $e->getMessage() . "<br/>" . PHP_EOL;
          }
        } else {
          echo "Stby<br/>" . PHP_EOL;
        }

        // Back to recipe details
        ?>
        <form action="<?php echo(basename(__FILE__)) ?>" method="post">
          <input type="hidden" name="operation" value="details">
          <input type="hidden" name="rec-id" value="<?php echo $rec_id ?>">
          <input type="submit" value="Back to Details">
        </form>

        <?php

      } else if ($operation == 'delete-ingredient') {
        $rec_id = $_POST['rec-id'];
        $ing_id = $_POST['ing-id'];
        // Delete ingredient from recipe
        echo "Will delete Ingredient " . $ing_id . " from Recipe " . $rec_id . "... <br/>" . PHP_EOL;

        $sql = 'DELETE FROM INGREDIENTS_PER_RECIPE WHERE RECIPE = ' . ($rec_id) . ' AND INGREDIENT = ' . ($ing_id) . ';';
        echo('Performing statement <code>' . $sql . '</code><br/>');
        if (true) { // Do perform ?
          try {
            $backend->connectDB("./sql/recipes.db");
            if ($VERBOSE) {
              echo("Connection created.<br/>". PHP_EOL);
            }
            $db = $backend->getDBObject();

            $db->exec($sql);
            echo "OK. Operation performed successfully<br/>" . PHP_EOL;
            // On ferme !
            $backend->closeDB();
            if ($VERBOSE) {
              echo("Closed DB<br/>" . PHP_EOL);
            }
          } catch (Throwable $e) {
            echo "Captured Throwable for exec() : " . $e->getMessage() . "<br/>" . PHP_EOL;
          }
        } else {
          echo "Stby<br/>" . PHP_EOL;
        }

        // Back to recipe details
        ?>
        <form action="<?php echo(basename(__FILE__)) ?>" method="post">
          <input type="hidden" name="operation" value="details">
          <input type="hidden" name="rec-id" value="<?php echo $rec_id ?>">
          <input type="submit" value="Back to Details">
        </form>

        <?php

      } else if ($operation == 'update') {
        $rec_id = $_POST['rec-id'];
        $rec_name = $_POST['rec-name'];
        echo "Will update Recipe " . $rec_id . " to name " . $rec_name . "... <br/>" . PHP_EOL;

        try {
          $backend->connectDB("./sql/recipes.db");
          if ($VERBOSE) {
            echo("Connection created.<br/>". PHP_EOL);
          }
          $db = $backend->getDBObject();

          $escapedName = str_replace("'", "''", $rec_name);
          $sql = 'UPDATE RECIPES SET NAME = \'' . $escapedName . '\' WHERE (RANK = ' . ($rec_id) . ')';
          echo('Performing statement <code>' . $sql . '</code><br/>');

          if (true) { // Do perform ?
            try {
              $db->exec($sql);
              echo "OK. Operation performed successfully<br/>" . PHP_EOL;
            } catch (Throwable $e) {
              echo "Captured Throwable for exec() : " . $e->getMessage() . "<br/>" . PHP_EOL;
            }
          } else {
            echo "Stby<br/>" . PHP_EOL;
          }
          // On ferme !
          $backend->closeDB();
          if ($VERBOSE) {
            echo("Closed DB<br/>".PHP_EOL);
          }
          ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="get">
          <input type="submit" value="Query Form">
        </form>
          <?php

        } catch (Throwable $e) {
          echo "Captured Throwable for connection : " . $e->getMessage() . "<br/>" . PHP_EOL;
        }

      } else if ($operation == 'create') {
        echo "Create a new Recipe. <br/>" . PHP_EOL;

        ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="post">
          <input type="hidden" name="operation" value="insert">
          <table>
            <tr>
              <td valign="top">Recipe :</td>
              <td>
                <input type="text" name="rec-name" size="40" required>
              </td>
            </tr>
            <tr>
              <td colspan="2" style="text-align: center;"><input type="submit" value="Insert"></td>
            </tr>
          </table>
        </form>
        <?php
      } else if ($operation == 'insert') {
        $rec_name = $_POST['rec-name'];
        echo "Will insert Recipe into RECIPES: " . $rec_name . "... <br/>" . PHP_EOL;

        try {
          $backend->connectDB("./sql/recipes.db");
          if ($VERBOSE) {
            echo("Connection created.<br/>". PHP_EOL);
          }
          $db = $backend->getDBObject();

          $escapedName = str_replace("'", "''", $rec_name);

          $sql = 'INSERT INTO RECIPES (NAME ) VALUES (\'' . ($escapedName) . '\')';
          echo('Performing statement <code>' . $sql . '</code><br/>');

          if (true) { // Do perform ?
            try {
              $db->exec($sql);
              echo "OK. Operation performed successfully<br/>" . PHP_EOL;
            } catch (Throwable $e) {
              echo "Captured Throwable for exec() : " . $e->getMessage() . "<br/>" . PHP_EOL;
            }
          } else {
            echo "Stby<br/>" . PHP_EOL;
          }
          // On ferme !
          $backend->closeDB();
          if ($VERBOSE) {
            echo("Closed DB<br/>".PHP_EOL);
          }
          ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="get">
          <input type="submit" value="Query Form">
        </form>

          <?php

        } catch (Throwable $e) {
          echo "Captured Throwable for connection : " . $e->getMessage() . "<br/>" . PHP_EOL;
        }
      }
    } else { // Then display the recipe query form
        ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="post">
          <input type="hidden" name="operation" value="query">
          <table>
            <tr>
              <td valign="top">Recipe (part of the name) :</td><td><input type="text" name="rec-name" size="40"></td>
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
    echo "[Captured Throwable (big) for recipes.php : " . $plaf . "] " . PHP_EOL;
}
?>
</body>
</html>