#!/bin/bash
command="java -jar web3-monitoring-agent.jar"
if [[ -z CONF ]]; then
  command="$command --spring.config.additional-location=$CONF"
fi

echo "Starting Web3 Monitoring Agent with command: $command"
eval $command
