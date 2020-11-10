#!/usr/bin/env bash
if [ $# -eq 0 ]; then
    echo "Usage: $0 peer# [0..2]"
    exit 1
fi
echo "Starting peer# $1"
JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-19.0.0/Contents/Home M2_HOME=/Applications/apache-maven-3.6.0 /Applications/apache-maven-3.6.0/bin/mvn -q exec:java -Dexec.mainClass="town.lost.examples.appreciation.benchmark.Peer" -Dexec.args="$1 0.0.0.0:10000,0.0.0.0:10001,0.0.0.0:10002"
