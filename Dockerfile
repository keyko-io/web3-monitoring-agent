FROM maven:3.6.3-jdk-11

WORKDIR /

COPY server /server
COPY core /core
COPY pom.xml /

RUN mvn package

RUN mv server/target/web3-monitoring-agent.jar web3-monitoring-agent.jar
RUN mv server/docker-scripts/start-monitoring-agent.sh start-monitoring-agent.sh

EXPOSE 8060
ENTRYPOINT ["/bin/sh", "-c"]
CMD ["/start-monitoring-agent.sh"]
