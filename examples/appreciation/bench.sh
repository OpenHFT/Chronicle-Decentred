#!/bin/bash

HOST=${1:-"localhost"}
if [[ "localhost" == "$HOST" ]]; then
    DIR=${2:-$(cd ../..;pwd)}
else
    DIR=${2:-"Chronicle-Decentred"}
fi

function run {
    xterm -geometry $1 -e "ssh ${HOST} \"bash --login -c '$2'\""
}


echo "Compiling @ ${HOST}:${DIR}"
ssh ${HOST} "bash --login -c 'cd ${DIR} && git pull --rebase && mvn clean install -Dmaven.test.skip'"

echo "Starting benchmark @ ${HOST}:${DIR}"
run 180x70+100+100 "cd ${DIR}/examples/appreciation; ./server.sh" &

sleep 5

run 180x70+1200+100 "cd ${DIR}/examples/appreciation; ./client.sh 0.0.0.0 1 2" &
run 180x70+2300+100 "cd ${DIR}/examples/appreciation; ./client.sh 0.0.0.0 2 1" &
