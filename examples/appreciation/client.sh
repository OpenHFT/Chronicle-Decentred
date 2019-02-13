#!/bin/bash

PARAMS="$@"
echo "Starting client with params ${PARAMS}"

mvn exec:java -Dexec.mainClass="town.lost.examples.appreciation.benchmark.Client" -Dexec.args="${PARAMS}"
