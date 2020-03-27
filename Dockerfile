FROM maven:3.6.3-jdk-11

WORKDIR /

COPY server /server
COPY core /core
COPY pom.xml /

RUN mvn package

ENV CONF ""
ENV ETHEREUM_NODE_URL=http://localhost:8545
ENV ETHEREUM_CLIENT_ADDRESS=0x0
ENV MONGO_DB_URI=mongodb://mongo:27017/w3m-monitoring
ENV ZOOKEEPER_ADDRESS=zookeeper:2181
ENV KAFKA_ADDRESSES=kafka:29092
ENV KAFKA_SCHEMAREGISTRY_URL=http://schema-registry:8081

RUN mv server/target/web3-monitoring-agent.jar web3-monitoring-agent.jar
RUN mv server/docker-scripts/start-monitoring-agent.sh start-monitoring-agent.sh

EXPOSE 8060
ENTRYPOINT ["/bin/sh", "-c"]
CMD ["/start-monitoring-agent.sh"]
