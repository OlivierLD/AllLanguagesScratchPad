<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>PHP, SQLite, Query pdf from DB</title>
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

<body style="background-color: rgba(255, 255, 255, 0.2); background-image: none;"> <!-- background="bground.jpg" style="min-height: 900px;"> -->
<h2>Querying Recipe document...</h2>

<?php

ini_set('memory_limit', '-1'); // Required for reloadOneStation (or its equivalent)

function query_document($db, $rank) {

    $doc = null;
    $sql = 'SELECT PDF FROM RECIPES WHERE RANK = :rank';
    // prepare statement
    $stmt = $db->prepare($sql);

    $stmt->bindValue(':rank', $rank, SQLITE3_INTEGER);

    // execute the SQL statement
    $result = $stmt->execute();
    while ($row = $result->fetchArray()) {
        if ($row) {
            $doc = $row[0];
        }
    }
    // return last queried id. Should be only one.
    return $doc;
}

$VERBOSE = false;

try {
    set_time_limit(3600); // In seconds. 300: 5 minutes, 3600: one hour
    // phpinfo();
    include __DIR__ . '/autoload.php';

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low.");
    }

    $backend = new BackEndSQLiteComputer();
    if ($VERBOSE) {
        echo("Backend created.<br/>". PHP_EOL);
    }
    // $backend->getStationsData();

    $backend->connectDB("./sql/recipes.db");
    if ($VERBOSE) {
        echo("Connection created.<br/>". PHP_EOL);
    }
    $db = $backend->getDBObject();

    echo("-------------------------------<br/>" . PHP_EOL);
    if (true) {
        // Getting the document back...
        // Also see https://phppot.com/php/mysql-blob-using-php/ for inspiration
        if ($VERBOSE) {
            echo("Getting the document back...<br/>" . PHP_EOL);
        }
        $rank = $_GET['recid'] ?? null;

        // $doc = query_document($db, 1);
        $doc = query_document($db, $rank);
        if ($doc) {
            $outFile = "./pdf/out-sample.pdf";
            // $writeResult = file_put_contents($outFile, $doc);

            $fhandle = fopen($outFile, "w");
            fwrite($fhandle, $doc);
            fclose($fhandle);

            if ($VERBOSE) {
                echo("Document written to file: " . $outFile . "<br/>" . PHP_EOL);
            }
            if (false /*$writeResult === false*/) {
                echo ("Could not write to file: " . $outFile . "<br/>" . PHP_EOL);
            } else {
                echo "Your pdf document is <a href='" . $outFile . "'>here</a>.<br/>" . PHP_EOL;
            }

            // echo "<iframe src='data:application/pdf;base64," .$doc . "' type='application/pdf' style='height:600px;width:60%;'></iframe><br/>" . PHP_EOL;
            // echo "<a href='data:application/pdf;base64," .$doc . "' type='application/pdf''>The pdf document</a><br/>" . PHP_EOL;

            // echo "<embed src='data:application/pdf;base64," .$doc . "' type='application/pdf' width='700px' height='500px' /><br/>" . PHP_EOL;

        } else {
            echo("No document found !<br/>" . PHP_EOL);
        }
    }

    $backend->closeDB();
    echo("-------------------------------<br/>" . PHP_EOL);
    echo("Connection closed.<br/>". PHP_EOL);
    echo("Query Completed.<br/>". PHP_EOL);

} catch (Throwable $plaf) {
    echo "[Captured Throwable (big) for query.doc.php : " . $plaf . "] " . PHP_EOL;
}
?>
</body>
</html>