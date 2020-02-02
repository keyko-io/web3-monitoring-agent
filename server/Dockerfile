FROM openjdk:11-jre
ADD target/web3-monitoring-agent.jar web3-monitoring-agent.jar
ADD docker-scripts/start-monitoring-agent.sh start-monitoring-agent.sh
ENV CONF ""
EXPOSE 8060
CMD chmod +x start-monitoring-agent.sh && ./start-monitoring-agent.sh
