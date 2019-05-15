#!/usr/bin/env bash
echo "Starting Traffic"
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home M2_HOME=/Applications/apache-maven-3.6.0 /Applications/apache-maven-3.6.0/bin/mvn -q exec:java -Dexec.mainClass="town.lost.examples.appreciation.benchmark.Traffic" -Dexec.args="0.0.0.0:10000 $1"
