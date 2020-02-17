FROM maven:3.6.3-jdk-11

WORKDIR /

COPY server /server
COPY core /core
COPY pom.xml /

RUN mvn package

RUN mv server/target/web3-monitoring-agent.jar web3-monitoring-agent.jar
RUN mv server/docker-scripts/start-monitoring-agent.sh start-monitoring-agent.sh

ENV CONF ""
ENV ETHEREUM_NODE_URL: https://baklava-forno.celo-testnet.org
ENV ETHEREUM_CLIENT_ADDRESS: 0x0
ENV SPRING_DATA_MONGODB_HOST: mongo:27017
ENV ZOOKEEPER_ADDRESS: zookeeper:2181
ENV KAFKA_ADDRESSES: kafka:9092
ENV KAFKA_SCHEMAREGISTRY_URL: http://schema-registry:8081
EXPOSE 8060
CMD chmod +x start-monitoring-agent.sh && ./start-monitoring-agent.sh