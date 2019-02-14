#!/bin/bash

REBUILD=NO
RUN=YES

PARAMS=()

while [[ $# -gt 0 ]]; do
    KEY="$1"
    case ${KEY} in
        -r|--rebuild)
        REBUILD=YES
        shift
        ;;
        -n|--norun)
        RUN=NO
        shift
        ;;
        -h|--help)
        echo "Usage: $0 <-r|--rebuild> <-n|--no-run> <host> <dir>"
        echo "  <-r|--rebuild> git pull and rebuild before running benchmark"
        echo "  <-n|--no-run>  do not run benchmark"
        echo "  <host>         name of host (defaults to localhost)"
        echo "  <dir>          location of Chronicle-Decentred root dir"
        echo
        echo "examples:"
        echo " Run benchmark on localhost:"
        echo "  $0"
        echo " Run benchmark on dev11:"
        echo "  $0 dev11"
        echo " Get dev11 updated to latest commits on origin/benchmark, but do not run benchmark"
        echo "  $0 -r -n dev11"
        echo
        echo "Running on a remote machine myhost probably requires you to do the following first:"
        echo "  ssh myhost git clone $(git remote get-url origin)"
        exit
        ;;
        *)
        PARAMS+=("$1")
        shift
        ;;
    esac
done

HOST=${PARAMS[0]:-"localhost"}
if [[ "localhost" == "$HOST" ]]; then
    DIR=${PARAMS[1]:-$(cd ../..;pwd)}
else
    DIR=${PARAMS[1]:-"Chronicle-Decentred"}
fi

function run {
    xterm -geometry $1 -e "ssh ${HOST} \"bash --login -c '$2'\""
}

if [[ ${REBUILD} == YES ]]; then
    echo "Compiling @ ${HOST}:${DIR}"
    ssh ${HOST} "bash --login -c 'cd ${DIR} && git checkout benchmark && git pull --rebase && mvn clean install -q -Dmaven.test.skip && cd examples/appreciation && mvn clean install -q -Dmaven.test.skip'"
fi

if [[ ${RUN} == YES ]]; then
    echo "Starting benchmark @ ${HOST}:${DIR}"
    run 180x70+100+100 "cd ${DIR}/examples/appreciation; ./server.sh" &

    sleep 5

    run 180x70+1200+100 "cd ${DIR}/examples/appreciation; ./client.sh 0.0.0.0 1 2" &
    run 180x70+2300+100 "cd ${DIR}/examples/appreciation; ./client.sh 0.0.0.0 2 1" &
fi