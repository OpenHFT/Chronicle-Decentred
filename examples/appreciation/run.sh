#!/bin/bash

CLASS=${1}
shift
PARAMS="$@"
echo "Starting ${CLASS} with params ${PARAMS}"

mvn -q exec:java -Dexec.mainClass="town.lost.examples.appreciation.benchmark.${CLASS}" -Dexec.args="${PARAMS}" 2>&1 | tee /tmp/run.$$.log

