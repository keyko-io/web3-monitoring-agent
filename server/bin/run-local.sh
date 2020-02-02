#!/usr/bin/env bash
set -euo pipefail

export W3M_HOST=localhost

export SPRING_DATA_MONGODB_HOST=$W3M_HOST:27017
export ETHEREUM_NODE_URL=http://$W3M_HOST:8545
export ZOOKEEPER_ADDRESS=$W3M_HOST:2181
export KAFKA_ADDRESSES=$W3M_HOST:9092

java -jar target/monitoring-agent-server.jar

