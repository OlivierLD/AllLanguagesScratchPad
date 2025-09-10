<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>PHP, SQLite Sample</title>
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
<h2>PHP various tests, and others...</h2>

<?php

ini_set('memory_limit', '-1'); // Required for reloadOneStation (or its equivalent)

function insert_document($db, $mimeType, $filename, $rank) {

    $sql = 'UPDATE RECIPES SET PDF = :doc WHERE RANK = :rank';

    $fh = fopen($filename, 'rb');
    if (!$fh) {
        throw new \Exception('Could not open file: ' . $filename);
    }

    // prepare statement
    $stmt = $db->prepare($sql);

    // $stmt->bindParam(':mime_type', $mimeType);
    $stmt->bindParam(':doc', $fh, SQLITE3_BLOB);
    $stmt->bindValue(':rank', $rank, SQLITE3_INTEGER);

    // execute the SQL statement
    $stmt->execute();

    // return last inserted id
    return;
}

function query_document($db, $rank) {

    $sql = 'SELECT PDF FROM RECIPES WHERE RANK = :rank';

    // $fh = fopen($filename, 'rb');
    // if (!$fh) {
    //     throw new \Exception('Could not open file: ' . $filename);
    // }
    $doc = null;

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

    // return last inserted id
    return $doc;
}

try {
    set_time_limit(3600); // In seconds. 300: 5 minutes, 3600: one hour
    // phpinfo();
    include __DIR__ . '/autoload.php';

    $VERBOSE = true;

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low.");
    }

    $backend = new BackEndSQLiteComputer();
    echo("Backend created.<br/>". PHP_EOL);

    // $backend->getStationsData();

    $backend->connectDB("./sql/recipes.db");
    echo("Connection created.<br/>". PHP_EOL);
    $db = $backend->getDBObject();


    if (true) {
        $year = (int)date("Y"); // gmdate ?
        $month = (int)date("m");
        $day = (int)date("d");
    }

    echo("-------------------------------<br/>" . PHP_EOL);
    if (false) {
        echo("Inserting a pdf in a BLOB...<br/>" . PHP_EOL);
        // insert_document($db, "application/pdf", "./sql/sample.pdf", 1);
        insert_document($db, "application/pdf", "./pdf/Gnocchis.pdf", 58);
        echo("-------------------------------<br/>" . PHP_EOL);
    }

    if (true) {
        // Getting the document back...
        // Also see https://phppot.com/php/mysql-blob-using-php/ for inspiration
        echo("Getting the document back...<br/>" . PHP_EOL);
        // $doc = query_document($db, 1);
        $doc = query_document($db, 58);
        if ($doc) {
            $outFile = "./sql/out-sample.pdf";
            file_put_contents($outFile, $doc);
            echo("Document written to file: " . $outFile . "<br/>" . PHP_EOL);

            echo "<a href='" . $outFile . "' target='_blank'>The pdf document</a><br/>" . PHP_EOL;

            // echo "<iframe src='data:application/pdf;base64," .$doc . "' type='application/pdf' style='height:600px;width:60%;'></iframe><br/>" . PHP_EOL;
            // echo "<a href='data:application/pdf;base64," .$doc . "' type='application/pdf''>The pdf document</a><br/>" . PHP_EOL;

            // echo "<embed src='data:application/pdf;base64," .$doc . "' type='application/pdf' width='700px' height='500px' /><br/>" . PHP_EOL;

        } else {
            echo("No document found !<br/>" . PHP_EOL);
        }
    }

    $backend->closeDB();
    echo("Connection closed.<br/>". PHP_EOL);

    echo("Test Completed.<br/>". PHP_EOL);

} catch (Throwable $plaf) {
    echo "[Captured Throwable (big) for sql.sample.php : " . $plaf . "] " . PHP_EOL;
}
?>
</body>
</html>