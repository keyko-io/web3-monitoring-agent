FROM openjdk:11-jre
ADD target/monitoring-agent-server.jar monitoring-agent-server.jar
ADD docker-scripts/start-monitoring-agent.sh start-monitoring-agent.sh
ENV CONF ""
EXPOSE 8060
CMD chmod +x start-monitoring-agent.sh && ./start-monitoring-agent.sh
