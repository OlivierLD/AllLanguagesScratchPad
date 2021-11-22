#!/bin/bash
echo -e "Smallest HTTP Server packaging script"
FROM_DIR=$(pwd)
echo -e "Packaging the server..."
rm -rf classes
rm -rf dist
mkdir classes
#
# No debug option (to keep it small)
# Remove the '-g:none' from the javac command to have it.
# Add -verbose if interested
#
javac -g:none -d classes -s src/main/java \
      src/main/java/http/httpserver/SmallestHTTPServer.java
mkdir dist
echo "Main-Class: http.httpserver.SmallestHTTPServer" > manifest.txt
echo "Compile-date: $(date)" >> manifest.txt
cd classes
jar -cvfm ../dist/smallest.http.jar ../manifest.txt *
#
echo -e "To run the server:"
echo -e "cd ../dist"
echo -e "java [-Dhttp.port:8888] -jar smallest.http.jar --verbose:false"
#
cd ${FROM_DIR}
rm manifest.txt
echo -e "------------------------"
echo -e "Done. Jar is ready:"
echo -e "------------------------"
ls -lisah dist
echo -e "------------------------"
