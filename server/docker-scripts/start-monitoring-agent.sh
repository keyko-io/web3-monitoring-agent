#!/bin/bash
command="java -jar monitoring-agent-server.jar"
if [[ -z CONF ]]; then
  command="$command --spring.config.additional-location=$CONF"
fi

echo "Starting Web3 Monitoring Agent with command: $command"
eval $command
