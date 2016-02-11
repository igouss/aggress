#!/bin/bash
export LIB=~/aggress/frontend/build/libs
export CLASSPATH=$(JARS=("$LIB"/*.jar); IFS=:; echo "${JARS[*]}")CLASSPATH=$(JARS=("$LIB"/*.jar); IFS=:; echo "${JARS[*]}")
java -cp $CLASSPATH$CLASSPATH com.naxsoft.Server

