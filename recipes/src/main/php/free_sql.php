<?php
// Must be on top
$timeout = 300;  // In seconds
$applyTimeout = false; // Change at will

try {
    set_time_limit(3600); // In seconds. 300: 5 minutes, 3600: one hour
    // phpinfo();
    include __DIR__ . '/autoload.php';

    $VERBOSE = false;

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low.");
    }

    if ($applyTimeout) {
      ini_set("session.gc_maxlifetime", $timeout);
      ini_set("session.cookie_lifetime", $timeout);
    }

} catch (Throwable $e) {
  echo "Session settings: Captured Throwable: " . $e->getMessage() . "<br/>" . PHP_EOL;
}
?>
<html lang="en">
  <!--
   ! Free SQL Statement
   +-->
  <head>
    <!--meta charset="UTF-8">
    <meta charset="ISO-8859-1"-->
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Free SQL</title>
    <style type="text/css">
      * {
        font-family: 'Courier New'
      }

      tr > td {
        border: 1px solid silver;
      }
    </style>
  </head>
  <body>
    <h1>PHP / SQLite. Free SQL</h1>
    <div>
      Try this: <code>SELECT sql FROM sqlite_schema WHERE name = 'recipes';</code>
      <br/>
      Or this: <code>SELECT * FROM sqlite_schema WHERE name = 'recipes';</code>
      <hr/>
    </div>

    <?php
// phpinfo();

$backend = new BackEndSQLiteComputer();
if ($VERBOSE) {
  echo("Backend created.<br/>". PHP_EOL);
}


if (isset($_POST['operation'])) {
  $operation = $_POST['operation'];
  if ($operation == 'execute') { // Then execute the statement
    try {
      $sql = $_POST['free-sql'];

      $database = "./sql/recipes.db";
      echo("Will connect on " . $database . " ...<br/>");
      $backend->connectDB($database);
      if ($VERBOSE) {
        echo("Connection created.<br/>". PHP_EOL);
      }
      $db = $backend->getDBObject();

      echo('Executing <code>' . $sql . '</code><br/>');

      $result = $db->query($sql);

      // echo "$result: " .  print_r($result, true) . "<br/>" . PHP_EOL;

      if ($result) {
        echo("<h2>Execution returned:</h2>");
        echo ("<table>" . PHP_EOL);
        while ($row = $result->fetchArray(SQLITE3_NUM)) {
          // var_dump($row, true);
          // echo("There are " . count($row) . " column(s).<br/>" . PHP_EOL);
          echo("<tr>" . PHP_EOL);
          for ($i=0; $i<count($row); $i++) {
            echo("<td>" . $row[$i] . "</td>" . PHP_EOL);
          }
          echo("</tr>" . PHP_EOL);
        }
        echo ("</table>" . PHP_EOL);

      } else {
        echo("Execution failed for <br/>" . $sql . "<br/>");
      }
      // On ferme !
      $backend->closeDB();
      echo("<hr>Closed DB<br/>" . PHP_EOL);
    } catch (Throwable $e) {
      echo "Captured Throwable for connection : " . $e->getMessage() . "<br/>" . PHP_EOL;
    }
    echo("<hr/>" . PHP_EOL);
    // echo("Again ? Click <a href='#'>Here</a>.");

    ?>
    <form action="" method="get">
      <input type="hidden" name="stmt" value="<?php echo($sql); ?>">
      <table>
        <tr>
          <td colspan="2" style="text-align: center;"><input type="submit" value="Another one ?"></td>
        </tr>
      </table>
    </form>
    <?php
  }
} else { // Then display the form
  if (isset($_GET['stmt'])) {
    $previousStmt = $_GET['stmt'];
  } else {
    $previousStmt = '';
  }
    ?>
    <form action="" method="post">
      <input type="hidden" name="operation" value="execute">
      <textarea id="free-sql" name="free-sql" rows="10" cols="80" placeholder="Your SQL Statement goes here"><?php echo($previousStmt); ?></textarea>
      <br/>
      <input type="submit" value="Execute">
    </form>
    <?php
}
    ?>
  </body>
</html>