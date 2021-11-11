#!/bin/bash
echo -e "Packaging Minimal HTTP Server!"
FROM_DIR=$(pwd)
echo -e "Packaging the server..."
rm -rf classes
rm -rf dist
mkdir classes
#
# No debug option (to keep it small)
# Remove the '-g:none' from the javac command to have it.
#
javac -g:none -d classes -s src/main/java \
      src/main/java/http/httpserver/StandaloneHTTPServer.java
mkdir dist
echo "Main-Class: http.httpserver.StandaloneHTTPServer" > manifest.txt
echo "Compile-date: $(date)" >> manifest.txt
cd classes
jar -cvfm ../dist/mini.http.jar ../manifest.txt *
#
echo -e "To run the server:"
echo -e "cd ../dist"
echo -e "java [-Dhttp.port:8888] -jar mini.http.jar --verbose:false"
echo -e "For help: java -jar mini.http.jar --help"
#
cd ${FROM_DIR}
rm manifest.txt
echo -e "Done."
