#!/bin/bash
FROM_DIR=$(pwd)
CP=./build/libs/oliv-scratch-pad-1.0-all.jar
java -cp ${CP} sql.OlivSQLPlus $*
