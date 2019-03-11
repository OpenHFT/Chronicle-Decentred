#!/bin/bash

PULL=NO
REBUILD=NO
RUN=YES

PARAMS=()

while [[ $# -gt 0 ]]; do
    KEY="$1"
    case ${KEY} in
        -p|--pull)
        PULL=YES
        shift
        ;;
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
        echo "  <-p|--pull>    git pull before running benchmark"
        echo "  <-r|--rebuild> rebuild before running benchmark"
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
    xterm -fa 'Monospace' -fs 14 -T "${1}" -geometry "${2}" -e "ssh ${HOST} \"bash --login -c 'cd ${DIR}/examples/appreciation; ./run.sh ${3}'\""
}

if [[ ${PULL} == YES ]]; then
    echo "Pulling latest @ ${HOST}:${DIR}"
    ssh ${HOST} "bash --login -c 'cd ${DIR} && git checkout benchmark && git pull --rebase'"
fi

if [[ ${REBUILD} == YES ]]; then
    echo "Compiling @ ${HOST}:${DIR}"
    ssh ${HOST} "bash --login -c 'cd ${DIR} && mvn clean install -q -Dmaven.test.skip && cd examples/appreciation && mvn clean install -q -Dmaven.test.skip'"
fi

if [[ ${RUN} == YES ]]; then
    echo "Starting benchmark @ ${HOST}:${DIR}"
    PEERS="0.0.0.0:10000,0.0.0.0:10001,0.0.0.0:10002"

    run "Peer 0" 112x50+10+10 "Peer 0 ${PEERS}" &
    run "Peer 1" 112x50+1300+10 "Peer 1 ${PEERS}" &
    run "Peer 2" 112x50+2590+10 "Peer 2 ${PEERS}" &

    sleep 15

    run "Traffic" 112x20+200+1200 "Traffic 0.0.0.0:10000" &

fi