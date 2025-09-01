<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Recipes</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <style type="text/css">
		* {
			font-family: 'Courier New', Courier, monospace;
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
    echo("Backend created.<br/>". PHP_EOL);

    // $backend->connectDB("./sql/recipes.db");
    // echo("Connection created.<br/>". PHP_EOL);

    if (isset($_POST['operation'])) {
      $operation = $_POST['operation'];
      if ($operation == 'query') { // Then do the query
        try {
          $rec_name = $_POST['rec-name'];

          $backend->connectDB("./sql/recipes.db");
          echo("Connection created.<br/>". PHP_EOL);

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
          echo "<tr><th>Rank</th><th>Recipe</th></tr>";

          for ($i=0; $i<count($recipes); $i++) {
            $recipe = $recipes[$i];
            echo(
              "<tr><td>" . urldecode($recipe->id) . "</td>" .
                  "<td>" . urldecode($recipe->name) . "</td>" .
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
          echo("Closed DB<br/>".PHP_EOL);
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
      }  else if ($operation == 'delete') {
        $rec_id = $_POST['rec-id'];
        echo "Will delete Recipe " . $rec_id . " from RECIPES... <br/>" . PHP_EOL;

        try {
            $backend->connectDB("./sql/recipes.db");
            echo("Connection created.<br/>". PHP_EOL);
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
          echo("Closed DB<br/>".PHP_EOL);
          ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="get">
          <input type="submit" value="Query Form">
        </form>
          <?php

        } catch (Throwable $e) {
          echo "Captured Throwable for connection : " . $e->getMessage() . "<br/>" . PHP_EOL;
        }
      }  else if ($operation == 'edit') {
        echo "Edit Recipe... <br/>" . PHP_EOL;
        $rec_id = $_POST['rec-id'];
        $rec_name = $_POST['rec-name'];
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
      }  else if ($operation == 'update') {
        $rec_id = $_POST['rec-id'];
        $rec_name = $_POST['rec-name'];
        echo "Will update Recipe " . $rec_id . " to name " . $rec_name . "... <br/>" . PHP_EOL;

        try {
            $backend->connectDB("./sql/recipes.db");
            echo("Connection created.<br/>". PHP_EOL);
            $db = $backend->getDBObject();

          $sql = 'UPDATE RECIPES SET NAME = \'' . ($rec_name) . '\' WHERE (RANK = ' . ($rec_id) . ')';
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
          echo("Closed DB<br/>".PHP_EOL);
          ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="get">
          <input type="submit" value="Query Form">
        </form>
          <?php

        } catch (Throwable $e) {
          echo "Captured Throwable for connection : " . $e->getMessage() . "<br/>" . PHP_EOL;
        }

      }  else if ($operation == 'create') {
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
      }  else if ($operation == 'insert') {
        $rec_name = $_POST['rec-name'];
        echo "Will insert Recipe into RECIPES: " . $rec_name . "... <br/>" . PHP_EOL;

        try {
            $backend->connectDB("./sql/recipes.db");
            echo("Connection created.<br/>". PHP_EOL);
            $db = $backend->getDBObject();

          $sql = 'INSERT INTO RECIPES (NAME ) VALUES (\'' . ($rec_name) . '\')';
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
          echo("Closed DB<br/>".PHP_EOL);
          ?>
        <form action="<?php echo(basename(__FILE__)); ?>" method="get">
          <input type="submit" value="Query Form">
        </form>

          <?php

        } catch (Throwable $e) {
          echo "Captured Throwable for connection : " . $e->getMessage() . "<br/>" . PHP_EOL;
        }
      }
    } else { // Then display the query form
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