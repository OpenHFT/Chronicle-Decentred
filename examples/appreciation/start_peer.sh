#!/usr/bin/env bash
if [ $# -eq 0 ]; then
    echo "Usage: $0 peer# [0..2]"
    exit 1
fi
echo "Starting peer# $1"
if [ ! -d target ] ; then
  mvn install
fi
mvn -q exec:java -Dexec.mainClass="town.lost.examples.appreciation.benchmark.Peer" -Dexec.args="$1 0.0.0.0:10000,0.0.0.0:10001,0.0.0.0:10002"
