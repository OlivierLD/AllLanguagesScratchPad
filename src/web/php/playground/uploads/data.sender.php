<?php
// Composite files uploads utility.
//
// Raw result of the upload operation
// The name(s) "fileToUpload-*" come(s) from the submitted form.
// From https://www.w3schools.com/php/php_file_upload.asp

// Create if needed, under 'data' folder.
function getTargetDir($subdir) {
  $target_dir = "data/"; // Under THIS directory
  $target_dir .= ($subdir . "/");

  if (is_dir($target_dir)) {
    echo "The directory $target_dir already exists.<br/>" . PHP_EOL;
  } else {
    echo "The directory $target_dir does not exist, creating it.<br/>" . PHP_EOL;
    mkdir($target_dir);
  }
  return $target_dir;
}

// Upload one file, in a given directory
function sendCompositeElement($dataArray, $subdir) {
  $target_dir = getTargetDir($subdir);

  $fileName = $dataArray["name"];
  if ($fileName == null || strlen($fileName) == 0) { // ie No file chosen
    echo "No file name was provided. Doing nothing.<br/>" . PHP_EOL;
    return;
  }

  $target_file = $target_dir . basename($fileName);
  $uploadOk = 1;  // ie true
  // TODO Make sure this is expected.
  $imageFileType = strtolower(pathinfo($target_file, PATHINFO_EXTENSION));

  // Check if image file is a actual image or fake image...
  if (false) {
    if (isset($_POST["submit"])) {
      $check = getimagesize($dataArray["tmp_name"]);
      if ($check !== false) {
        echo "File is an image - " . $check["mime"] . ".<br/>" . PHP_EOL;
        $uploadOk = 1;
      } else {
        echo "File is not an image.<br/>" . PHP_EOL;
        $uploadOk = 0;
      }
    }
  }

  // Check if file already exists
  if (file_exists($target_file)) {
    echo "Sorry, file already exists.<br/>" . PHP_EOL;
    $uploadOk = 0;
  }

  // Check file size. 1Mb max...
  if ($dataArray["size"] > (1 * 1024 * 1024)) {  // ~1 Mb
    echo "Sorry, your file is too large (it's (" . $dataArray["size"] . " big).<br/>" . PHP_EOL;
    $uploadOk = 0;
  }

  if (false) {
    // Allow certain file formats only. Disabled with false
    if ($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg" && $imageFileType != "gif") {
      echo "Sorry, only JPG, JPEG, PNG & GIF files are allowed.<br/>" . PHP_EOL;
      $uploadOk = 0;
    }
  }

  // Check if $uploadOk is set to 0 by an error
  if ($uploadOk == 0) {
    echo "Your file will not be uploaded.<br/>" . PHP_EOL;
  } else { // if everything is ok, try to upload file
    if (move_uploaded_file($dataArray["tmp_name"], $target_file)) {
      echo "The file " . htmlspecialchars(basename($dataArray["name"])). " has been uploaded to $target_dir.<br/>" . PHP_EOL;
    } else {
      echo "There was an error uploading your file.<br/>" . PHP_EOL;
      // TODO More on the error ?
      die("Oops");
    }
  }
}

function sendJson($jsonContent, $subdir, $fileName) {

  $target_dir = getTargetDir($subdir);
  $file = $target_dir . $fileName;

  // Write the contents to the file,
  // using the FILE_APPEND flag to append the content to the end of the file
  // and the LOCK_EX flag to prevent anyone else writing to the file at the same time
  file_put_contents($file, $jsonContent, FILE_APPEND | LOCK_EX);
  echo("File $file was written<br/>" . PHP_EOL);
}

/*
 * Story begins here.
 */

echo "-------------------<br/>" . PHP_EOL;
var_dump($_POST);
echo "-------------------<br/>" . PHP_EOL;

$operation = "uploads";
if (isset($_POST["operation"])) {
  $operation = $_POST["operation"];
}
$subdir = uniqid(); // Used as composite name
if (isset($_POST["composite-name"])) {
  $subdir = $_POST["composite-name"];
}

if ($operation == "uploads") {
  echo "-------------------<br/>" . PHP_EOL;
  var_dump($_FILES);
  echo "-------------------<br/>" . PHP_EOL;

  echo "Will upload files in $subdir. <br/>" . PHP_EOL;

  $keepLooping = true;
  $fieldIdx = 1;

  // Loop on the $_FILES["fileToUpload-*"]
  while ($keepLooping) {
    $fieldId = "fileToUpload-$fieldIdx";
    if (isset($_FILES[$fieldId])) {
      $array = $_FILES[$fieldId];
      if ($array != null) {
        echo "-------------------------<br/>" . PHP_EOL;
        echo 'Received:<br/>' . PHP_EOL;
        echo '<pre>' . PHP_EOL;
        print_r($array);
        echo '</pre>' . PHP_EOL;
        echo '<br/>' . PHP_EOL;

        sendCompositeElement($array, $subdir);
        $fieldIdx++;
      } else {
        echo "No file for fileToUpload-$fieldIdx<br/>" . PHP_EOL;
      }
    } else {
      $keepLooping = false;
    }
  }

  echo "----------------------------<br/>" . PHP_EOL;
  echo "Done for now<br/>" . PHP_EOL;
} else if ($operation == "send-json") {
  echo "Will send a json file in $subdir<br/>" . PHP_EOL;
  $fileName = $_POST["file-name"];
  $jsonContent = $_POST["json-content"];
  $obj = json_decode($jsonContent);
  if ($obj != null) {
    $minified = json_encode($obj);
    if ($minified) {
      sendJson($minified, $subdir, $fileName);
    } else {
      echo "Oops... Obj cannot be encoded<br/>" . PHP_EOL;
      echo "----------------------------<br/>" . PHP_EOL;
      var_dump($obj);
      echo "----------------------------<br/>" . PHP_EOL;
    }
  } else {
    echo "Invalid JSON Obj $jsonContent<br/>" . PHP_EOL;
    echo "No file was created<br/>" . PHP_EOL;
    die("Boom!");
  }
}


?>
