<?php
// Raw result of the upload operation
// The name "fileToUpload" comes from the submitted form.
// From https://www.w3schools.com/php/php_file_upload.asp

function doYourJob($dataArray, $subdir) {
  $target_dir = "uploads/"; // Under THIS current directory

  $fileName = $dataArray["name"];
  if (strlen($fileName) == 0) {
    echo "No file name was provided. Doing nothing.<br/>" . PHP_EOL;
    return;
  }

  if (true) { // Create random-named directory
    $target_dir .= ($subdir . "/");

    if (is_dir($target_dir)) {
      echo "The directory $target_dir already exists.<br/>" . PHP_EOL;
    } else {
      echo "The directory $target_dir does not exist, creating it.<br/>" . PHP_EOL;
      mkdir($target_dir);
    }
  }

  $target_file = $target_dir . basename($fileName);
  $uploadOk = 1;  // ie true
  $imageFileType = strtolower(pathinfo($target_file, PATHINFO_EXTENSION));

  // Check if image file is a actual image or fake image
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

  // Check file size
  if ($dataArray["size"] > (1 * 1024 * 1024)) {  // ~1 Mb
    echo "Sorry, your file is too large (it's (" . $dataArray["size"] . " big).<br/>" . PHP_EOL;
    $uploadOk = 0;
  }

  // Allow certain file formats only. Disabled with false
  if (false) {
    if ($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg" && $imageFileType != "gif") {
      echo "Sorry, only JPG, JPEG, PNG & GIF files are allowed.<br/>" . PHP_EOL;
      $uploadOk = 0;
    }
  }

  // Check if $uploadOk is set to 0 by an error
  if ($uploadOk == 0) {
    echo "Sorry, your file was not uploaded.<br/>" . PHP_EOL;
  // if everything is ok, try to upload file
  } else {
    if (move_uploaded_file($dataArray["tmp_name"], $target_file)) {
      echo "The file " . htmlspecialchars(basename($dataArray["name"])). " has been uploaded to $target_dir.<br/>" . PHP_EOL;
    } else {
      echo "Sorry, there was an error uploading your file.<br/>" . PHP_EOL;
    }
  }
}

/*
 * Story begins here.
 */
echo "-------------------<br/>" . PHP_EOL;
var_dump($_FILES);
echo "-------------------<br/>" . PHP_EOL;

$subdir = uniqid();
echo "Will upload files in $subdir. <br/>" . PHP_EOL;

$array = $_FILES["fileToUpload"];
// echo "Received: [" . $_FILES["fileToUpload"] . "]<br/>" . PHP_EOL;
echo 'Received:<br/>' . PHP_EOL;
echo '<pre>' . PHP_EOL;
print_r($array);
echo '</pre>' . PHP_EOL;
echo '<br/>' . PHP_EOL;

doYourJob($array, $subdir);

$array = $_FILES["fileToUpload-2"];
// echo "Received: [" . $_FILES["fileToUpload"] . "]<br/>" . PHP_EOL;
echo 'Received:<br/>' . PHP_EOL;
echo '<pre>' . PHP_EOL;
print_r($array);
echo '</pre>' . PHP_EOL;
echo '<br/>' . PHP_EOL;

doYourJob($array, $subdir);

echo "----------------------------<br/>" . PHP_EOL;
echo "Done for now<br/>" . PHP_EOL;

?>
