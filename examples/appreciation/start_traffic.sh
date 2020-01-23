#!/usr/bin/env bash
echo "Starting Traffic"
if [ ! -d target ] ; then
  mvn install
fi
mvn -q exec:java -Dexec.mainClass="town.lost.examples.appreciation.benchmark.Traffic" -Dexec.args="0.0.0.0:10000 $1"
