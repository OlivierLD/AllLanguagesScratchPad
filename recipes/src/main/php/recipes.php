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

    .box {
        background-color: white;
        outline: 2px dashed black;
        /* height: 400px; */
    }
    .box.is-dragover {
        background-color: grey;
    }

    .box {
        display:flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }

    .box label strong {
        text-decoration: underline;
        color: blue;
        cursor: pointer;
    }

    .box label strong:hover {
        color: blueviolet
    }

    .box input {
        display: none;
    }
  </style>

</head>

<body style="background-color: rgba(255, 255, 255, 0.2); background-image: none;">
<!--h1>Recipes DB</h1-->
<h2>Recipes, and others...</h2>

<?php

$VERBOSE = false;

ini_set('memory_limit', '-1'); // Required for memory consuming operations...
// Ti avoid the  PHP Warning:  POST Content-Length of 10085720 bytes exceeds the limit of 8388608 bytes in Unknown on line 0
// ini_set('upload_max_filesize', '1000M');
ini_set('upload_max_filesize', '-1');
// ini_set('post_max_size', '1000M');
ini_set('post_max_size', '-1');

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
          $sort_by = $_POST['sort-by'];

          $backend->connectDB("./sql/recipes.db");
          if ($VERBOSE) {
            echo("Connection created.<br/>". PHP_EOL);
          }

          $recipes = $backend->buildRecipeList($rec_name, $sort_by);
          echo("Returned " . count($recipes) . " row(s)<br/>");

          echo("<h2>Recipes</h2>");

          ?> <!-- To Recipe creation form -->

          <table>
            <tr>
              <td>
                <form action="<?php echo(basename(__FILE__)); ?>" method="post">
                  <input type="hidden" name="operation" value="create">
                  <input type="submit" value="Create New Recipe">
                </form>
              </td>
              <td>
                <form action="<?php echo(basename(__FILE__)); ?>" method="get">
                  <input type="submit" value="Query Again ?">
                </form>
              </td>
            </tr>
          </table>

          <?php

          echo "<table>";
          echo "<tr><th>Rank</th><th>Recipe</th><th>Nb Ingredients</th><th>Preference</th><th>pdf</th></tr>";

          for ($i=0; $i<count($recipes); $i++) {
            $recipe = $recipes[$i];
            echo(
              "<tr><td>" . urldecode($recipe->id) . "</td>" .
                  "<td>" . urldecode($recipe->name) . "</td>" .
                  "<td>" . $recipe->nb_ing . "</td>" .
                  "<td>" . $recipe->pref_level . "</td>" .
                  "<td>" . ($recipe->pdf ? '<a href="query.doc.php?recid=' . urldecode($recipe->id) . '" onclick="" target="_blank">pdf Doc</a>' : ' - ') . "</td>" .
                  "<td>" .
                     "<form action=\"" . basename(__FILE__) . "\" method=\"post\">" .
                        "<input type=\"hidden\" name=\"operation\" value=\"edit\">" .
                        "<input type=\"hidden\" name=\"rec-id\" value=\"" . $recipe->id . "\">" .
                        "<input type=\"hidden\" name=\"rec-name\" value=\"" . urldecode($recipe->name) . "\">" .
                        "<input type=\"hidden\" name=\"pref-level\" value=\"" . $recipe->pref_level . "\">" .
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
              } catch (SQLite3Exception $sqlEx) {
                echo "Captured Exception for exec() : " . $sqlEx->getMessage() . "<br/>" . PHP_EOL;
                echo ("Error Code: " . $sqlEx->getCode() . "<br/>" . PHP_EOL);
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
        $pref_level = $_POST['pref-level'];
        echo "Edit Recipe [" . $rec_name . "]... <br/>" . PHP_EOL;
        ?>
        <table>
          <tr>
            <td>
              <form action="<?php echo(basename(__FILE__)); ?>" method="post">
                <input type="hidden" name="operation" value="update">
                <input type="hidden" name="rec-id" value="<?php echo($rec_id); ?>">
                <table>
                  <tr>
                    <td valign="top">Recipe :</td><td><input type="text" name="rec-name" size="40" value="<?php echo($rec_name); ?>"></td>
                  </tr>
                  <tr>
                    <td valign="top">Preference level :</td><td><input type="number" name="pref-level" size="5" value="<?php echo($pref_level); ?>"></td>
                  </tr>
                  <tr>
                    <td colspan="2" style="text-align: center;"><input type="submit" name="submit-type" value="Update"></td>
                  </tr>
                </table>
              </form>
            </td>
            <td>
              <form action="<?php echo(basename(__FILE__)); ?>" method="post" enctype="multipart/form-data"> <!-- enctype needed for file upload -->
                <input type="hidden" name="operation" value="upload">
                <input type="hidden" name="rec-id" value="<?php echo($rec_id); ?>">
                <table>
                  <tr>
                    <td valign="top">
                      <div class="box">
                        <label>
                          For Recipe "<?php echo($rec_name); ?>",<br/>
                          <strong>Choose pdf</strong> to upload (or drag here, if it works...)<br/>
                          <input class="box__file" type="file" name="files[]" multiple/>
                          <br/>
                          Click the "Upload" button when file is chosen.
                        </label>
                        <div class="file-list"></div>

                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td><input type="submit" name="submit-type" value="Upload"></td>
                  </tr>
                </table>
              </form>
            </td>
          </tr>
        </table>

        <!-- Back to Query -->
        <form action="<?php echo(basename(__FILE__)); ?>" method="get">
          <table>
            <tr>
              <td colspan="2" style="text-align: center;"><input type="submit" value="Back to recipes query"></td>
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
          <input type="text" name="ing-filter" value="<?php echo($ing_filter); ?>" id="ing-filter" placeholder="Filter on Ingredients">
          <input type="submit" value="Filter on Ingredients List">
          (joker is '%')
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

        <script type="text/javascript">
          setTimeout(() => {
            let filter = document.getElementById("ing-filter");
            if (filter) {
              filter.focus();
            } else {
              console.log('Filter not found...')
            }
          }, 500);
        </script>

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

            // Sumbit automatically if everything went well
            ?>
            <script type="text/javascript">
              // alert('Yo! It worked!');
              setTimeout(function(){
                document.getElementById("back-to-details").submit();
              }, 1000);

            </script>
            <?php

            // On ferme !
            $backend->closeDB();
            if ($VERBOSE) {
              echo("Closed DB<br/>" . PHP_EOL);
            }
          } catch (SQLite3Exception $sqlEx) {
            echo "Captured Exception for exec() : " . $sqlEx->getMessage() . "<br/>" . PHP_EOL;
            echo ("Error Code: " . $sqlEx->getCode() . "<br/>" . PHP_EOL);
          } catch (SQLite3Exception $sqlEx) {
            echo "Captured Exception for exec() : " . $sqlEx->getMessage() . "<br/>" . PHP_EOL;
            echo ("Error Code: " . $sqlEx->getCode() . "<br/>" . PHP_EOL);
          } catch (Throwable $e) {
            echo "Captured Throwable for exec() : " . $e->getMessage() . "<br/>" . PHP_EOL;
          }
        } else {
          echo "Stby<br/>" . PHP_EOL;
        }

        // Back to recipe details
        ?>
        <form action="<?php echo(basename(__FILE__)) ?>" id="back-to-details" method="post">
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
          } catch (SQLite3Exception $sqlEx) {
            echo "Captured Exception for exec() : " . $sqlEx->getMessage() . "<br/>" . PHP_EOL;
            echo ("Error Code: " . $sqlEx->getCode() . "<br/>" . PHP_EOL);
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

      } else if ($operation == 'upload') {

        $rec_id = $_POST['rec-id'];

        if (false) {
          echo ("-----------------------------<br/>" . PHP_EOL);
          var_dump($_POST);
          var_dump($_FILES);
          echo ("-----------------------------<br/>" . PHP_EOL);
        }

        if (isset($_POST['submit-type']) && $_POST['submit-type'] == 'Upload' && isset($_FILES['files'])) {
          echo "Will upload file...<br/>" . PHP_EOL;
          $uploaded_filenames = "";
          $target_dir = "./pdf/"; // Default origin...

          $backend->connectDB("./sql/recipes.db");
          if ($VERBOSE) {
            echo("Connection created.<br/>". PHP_EOL);
          }
          $db = $backend->getDBObject();

          for ($i=0; $i<count($_FILES['files']['name']); $i++) {
              // $filename = $_FILES['files']['name'][$i];
              $filename = $target_dir . basename($_FILES['files']['name'][$i]); // We need to know where (which dir) the file comes from !!!
              if ($_FILES['files']['tmp_name'][$i] && strlen($_FILES['files']['tmp_name'][$i]) > 0) { // Not always working if dragging, only if using the file selector
                  $filename = $_FILES['files']['tmp_name'][$i]; // This is the real file to read
              }

              $filetype = $_FILES['files']['type'][$i];

              $fh = fopen($filename, 'rb');
              if (!$fh) {
                  throw new Exception('Could not open file: ' . $filename);
              }
              $content = fread($fh, filesize($filename));
              fclose($fh);
              echo("File " . $filename . " of type " . $filetype . " has " . strlen($content) . " bytes.<br/>" . PHP_EOL);
              // echo("Content: <pre>" . htmlspecialchars($content) . "</pre><br/>" . PHP_EOL);

              // Next step: store in DB as BLOB
              // See https://www.sqlitetutorial.net/sqlite-php/sqlite-blob/
              // See in sql.sample.php.

              $backend->insert_document($db, $filetype /*"application/pdf"*/, $filename, $rec_id);
              ?>
              <form action="<?php echo(basename(__FILE__)); ?>" method="get">
                <table>
                  <tr>
                    <td colspan="2" style="text-align: center;"><input type="submit" value="Back to recipes query"></td>
                  </tr>
                </table>
              </form>
              <?php
          }
          // On ferme !
          $backend->closeDB();
        }

      } else if ($operation == 'update') {

        if (false) {
          echo ("-----------------------------<br/>" . PHP_EOL);
          var_dump($_POST);
          echo ("-----------------------------<br/>" . PHP_EOL);
        }

        if (isset($_POST['submit-type']) && $_POST['submit-type'] == 'Update') {

          $rec_id = $_POST['rec-id'];
          $rec_name = $_POST['rec-name'];
          $pref_level = $_POST['pref-level'];
          echo "Will update Recipe " . $rec_id . " to name " . $rec_name . ", level [" . $pref_level . "]... <br/>" . PHP_EOL;

          try {
            $backend->connectDB("./sql/recipes.db");
            if ($VERBOSE) {
              echo("Connection created.<br/>". PHP_EOL);
            }
            $db = $backend->getDBObject();

            $escapedName = str_replace("'", "''", $rec_name);
            $sql = 'UPDATE RECIPES SET NAME = \'' . $escapedName . '\', PREFERENCE_LEVEL = ' . ($pref_level == '' ? 'NULL' : $pref_level) . ' WHERE (RANK = ' . ($rec_id) . ')';
            echo('Performing statement <code>' . $sql . '</code><br/>');

            if (true) { // Do perform ?
              try {
                $db->exec($sql);
                echo "OK. Operation performed successfully<br/>" . PHP_EOL;
              } catch (SQLite3Exception $sqlEx) {
                echo "Captured Exception for exec() : " . $sqlEx->getMessage() . "<br/>" . PHP_EOL;
                echo ("Error Code: " . $sqlEx->getCode() . "<br/>" . PHP_EOL);
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

          $sql = 'INSERT INTO RECIPES (NAME) VALUES (\'' . ($escapedName) . '\')';
          echo('Performing statement <code>' . $sql . '</code><br/>');
          $createdRecId = -1;

          if (true) { // Do perform ?
            try {
              $db->exec($sql);
              echo "OK. Operation performed successfully<br/>" . PHP_EOL;

              $sql = 'SELECT RANK FROM RECIPES WHERE NAME = \'' . ($escapedName) . '\'';
              $results = $db->query($sql);
              $createdRecId = $results->fetchArray()[0]; // Assume one and only one row...
            } catch (SQLite3Exception $sqlEx) {
              echo "Captured Exception for exec() : " . $sqlEx->getMessage() . "<br/>" . PHP_EOL;
              echo ("Error Code: " . $sqlEx->getCode() . "<br/>" . PHP_EOL);
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
        <table>
          <tr>
            <td>
              <form action="<?php echo(basename(__FILE__)); ?>" method="get">
                <input type="submit" value="Query Form">
              </form>
            </td>

        <?php
          if ($createdRecId != -1) {
        ?>
            <td>
              <form action="<?php echo(basename(__FILE__)); ?>" method="post">
                <input type="hidden" name="operation" value="details">
                <input type="hidden" name="rec-id" value="<?php echo $createdRecId; ?>">
                <input type="submit" value="Add Details">
              </form>
            </td>
        <?php
          }
          ?>
          </tr>
        </table>
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
              <td valign="top">Recipe (part of the name) :</td>
              <td><input type="text" name="rec-name" size="40"></td>
            </tr>
            <tr>
              <td>
                Sort by
              </td>
              <td> <!-- values are positions in the select stmt -->
                <input type="radio" name="sort-by" value="1" id="rank"><label for="rank">Rank</label>
                <input type="radio" name="sort-by" value="2" id="name" checked><label for="name">Name</label>
                <input type="radio" name="sort-by" value="5" id="nb-ing"><label for="nb-ing">Nb Ingredients</label>
                <input type="radio" name="sort-by" value="3" id="pref-level"><label for="pref-level">Pref Level</label>
              </td>
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

    <script>

        const box = document.querySelector('.box');
        const fileInput = document.querySelector('[name="files[]"');
        const selectButton = document.querySelector('label strong');
        const fileList = document.querySelector('.file-list');

        let droppedFiles = [];

        try {
          [ 'drag', 'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave', 'drop' ]
              .forEach( event => {
                if (box) {
                  box.addEventListener(event, function(e) {
                      e.preventDefault();
                      e.stopPropagation();
                  })
                }
              }, false );
        } catch (err) {
          console.log('Error setting drag events: ' + err);
        }
        try {
          [ 'dragover', 'dragenter' ].forEach( event => box.addEventListener(event, function(e) {
              box.classList.add('is-dragover');
          }), false );

          [ 'dragleave', 'dragend', 'drop' ].forEach( event => box.addEventListener(event, function(e) {
              box.classList.remove('is-dragover');
          }), false );

          box.addEventListener('drop', function(e) {
              droppedFiles = e.dataTransfer.files;
              fileInput.files = droppedFiles;
              updateFileList();
          }, false );
        } catch (err) {
          console.log('Error setting drag events 2: ' + err);
        }

        try {
          fileInput.addEventListener( 'change', updateFileList );
        } catch (err) {
          console.log('Error setting file input change event: ' + err);
        }

        function updateFileList() {
            const filesArray = Array.from(fileInput.files);
            if (filesArray.length > 1) {
                fileList.innerHTML = '<p>Selected files:</p><ul><li>' + filesArray.map(f => f.name).join('</li><li>') + '</li></ul>';
            } else if (filesArray.length == 1) {
                fileList.innerHTML = `<p>Selected file: ${filesArray[0].name}</p>`;
            } else {
                fileList.innerHTML = '';
            }
        }

    </script>

</body>
</html>