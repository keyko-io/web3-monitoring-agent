#!/usr/bin/env bash
set -euo pipefail

#Thanks to Gregoire Jeanmart for this script
echo "Removing old containers"
docker rm -f server_kafka_1 server_zookeeper_1 server_mongodb_1 server_eventeum_1 server_parity_1 || echo -e "Containers removed"
docker-compose down

echo "Removing storage"
rm -rf $HOME/.mongodb/data
rm -rf $HOME/.parity/data
rm -rf $HOME/.parity/log

composescript="docker-compose.yml"

if [ "$1" = "rinkeby" ]; then
   composescript="docker-compose-rinkeby.yml"
   echo "Running in Rinkeby Infura mode..."
elif [ "$1" = "infra" ]; then
   composescript="docker-compose-infra.yml"
   echo "Running in Infrastructure mode..."
fi

docker-compose -f "$composescript" build
[ $? -eq 0 ] || exit $?;


echo "Start"
docker-compose -f "$composescript" up
[ $? -eq 0 ] || exit $?;

trap "docker-compose -f "$composescript" kill" INT
