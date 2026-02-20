<?php
// Raw result of the upload operation
// The name "fileToUpload" comes from the submitted form.
// From https://www.w3schools.com/php/php_file_upload.asp
$array = $_FILES["fileToUpload"];
// echo "Received: [" . $_FILES["fileToUpload"] . "]<br/>" . PHP_EOL;
echo 'Received:<br/>' . PHP_EOL;
echo '<pre>' . PHP_EOL;
print_r($array);
echo '</pre>' . PHP_EOL;
echo '<br/>' . PHP_EOL;

echo "-------------------<br/>" . PHP_EOL;
var_dump($_FILES);
echo "-------------------<br/>" . PHP_EOL;

$target_dir = "uploads/"; // Under THIS current directory

if (true) { // Create random-named directory
  $subdir = uniqid();
  $target_dir .= ($subdir . "/");

  if (is_dir($target_dir)) {
    echo "The directory exists.<br/>" . PHP_EOL;
  } else {
    echo "The directory does not exist.<br/>" . PHP_EOL;
    mkdir($target_dir);
  }
}

$target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
$uploadOk = 1;  // ie true
$imageFileType = strtolower(pathinfo($target_file, PATHINFO_EXTENSION));

// Check if image file is a actual image or fake image
if (false) {
  if (isset($_POST["submit"])) {
    $check = getimagesize($_FILES["fileToUpload"]["tmp_name"]);
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
if ($_FILES["fileToUpload"]["size"] > (1 * 1024 * 1024)) {  // ~1 Mb
  echo "Sorry, your file is too large (it's (" . $_FILES["fileToUpload"]["size"] . " big).<br/>" . PHP_EOL;
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
  if (move_uploaded_file($_FILES["fileToUpload"]["tmp_name"], $target_file)) {
    echo "The file " . htmlspecialchars(basename($_FILES["fileToUpload"]["name"])). " has been uploaded.<br/>" . PHP_EOL;
  } else {
    echo "Sorry, there was an error uploading your file.<br/>" . PHP_EOL;
  }
}
?>
