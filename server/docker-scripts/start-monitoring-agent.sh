#!/bin/bash
command="java -jar web3-monitoring-agent.jar --spring.config.location=$CONF"
echo "Starting Web3 Monitoring Agent with command: $command "
eval $command
