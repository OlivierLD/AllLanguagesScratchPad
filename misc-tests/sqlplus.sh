#!/bin/bash
CP=build/libs/some-tests-1.0-all.jar
java -cp ${CP} sql.OlivSQLPlus $*
