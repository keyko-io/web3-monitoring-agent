# Keyko Web3 Monitoring Agent

Table of Contents
=================

   * [Keyko Web3 Monitoring Agent](#keyko-web3-monitoring-agent)
   * [Table of Contents](#table-of-contents)
      * [Features](#features)
      * [Getting Started](#getting-started)
         * [Prerequisites](#prerequisites)
         * [Build](#build)
         * [Run](#run)
      * [Configuring Nodes](#configuring-nodes)
      * [Api](#api)
      * [Configuration](#configuration)
         * [INFURA Support Configuration](#infura-support-configuration)
      * [Advanced](#advanced)
         * [Correlation Id Strategies (Kafka Broadcasting)](#correlation-id-strategies-kafka-broadcasting)
         * [Event Store](#event-store)
            * [MongoDB](#mongodb)
            * [REST Service](#rest-service)
      * [Metrics: Prometheus](#metrics-prometheus)
      * [Known Caveats / Issues](#known-caveats--issues)
      * [Attribution](#attribution)
      * [License](#license)


---

Keyko Web3 Monitoring agent provides an intelligent software able to ingest blockchain information into Kafka. 
It listens for specified event emissions from the Ethereum network, and broadcasts these events into your middleware layer. 
It's also prepared to ingest network blocks and transactions. 
This provides a distinct separation of concerns and means that your microservices do not have to subscribe to events directly to an Ethereum node.

![W3M Agent Build](https://github.com/keyko-io/web3-monitoring-agent/workflows/W3M%20Agent%20Build/badge.svg)

## Features

* Dynamically Configurable - It exposes a REST api so that smart contract events can be dynamically subscribed / unsubscribed.

* Highly Available - The instances communicate with each other to ensure that every instance is subscribed to the same collection of smart contract events.

* Resilient - Node failures are detected and event subscriptions will continue from the failure block once the node comes back online.

* Fork Tolerance - It can be configured to wait a certain amount of blocks before an event is considered 'Confirmed'.  If a fork occurs during this time, a message is broadcast to the network, allowing your services to react to the forked/removed event.

## Getting Started

Follow the instructions below in order to run the agent on your local machine for development and testing purposes.

### Prerequisites

* Java 11
* Maven
* Docker (optional)

### Build

1. After checking out the code, navigate to the root directory
```
cd /path/to/agent/directory/
```

2. Compile, test and package the project

```
mvn clean package
```

### Run

a. If you have a running instance of MongoDB, Kafka, Zookeeper and an Ethereum node:

**Executable JAR:**

```sh
cd server
export SPRING_DATA_MONGODB_HOST=<mongodb-host:port>
export ETHEREUM_NODE_URL=http://<node-host:port>
export ZOOKEEPER_ADDRESS=<zookeeper-host:port>
export KAFKA_ADDRESSES=<kafka-host:port>

java -jar target/monitoring-agent-server.jar
```

Connecting to mainnet:
```bash
ETHEREUM_NODE_URL=wss://main-rpc.linkpool.io/ws java -jar server/target/web3-monitoring-agent-*.jar --spring.config.location=file:server/src/main/resources/application.yml
```

**Docker:**

```sh
$ cd server
$ docker build  . -t kauri/eventeum:latest

$ export SPRING_DATA_MONGODB_HOST=<mongodb-host:port>
$ export ETHEREUM_NODE_URL=http://<node-host:port>
$ export ZOOKEEPER_ADDRESS=<zookeeper-host:port>
$ export KAFKA_ADDRESSES=<kafka-host:port>

$ docker run -p 8060:8060 kauri/eventeum
```

b. If you prefer build an all-in-one test environment with a parity dev node, use docker-compose:

```sh
$ cd server
$ docker-compose -f docker-compose.yml build
$ docker-compose -f docker-compose.yml up
```

## Configuring Nodes
Listening for events from multiple different nodes is supported, and these nodes can be configured in the properties file.

```yaml
ethereum:
  nodes:
    - name: default
      url: http://mainnet:8545
    - name: sidechain
      url: wss://sidechain/ws
```

If an event does not specify a node, then it will be registered against the 'default' node.

That is the simplest node configuration, but there is other custom flags you can activate per node:


- `maxIdleConnections`: Maximum number of connections to the node. (default: 5)
- `keepAliveDuration`: Duration of the keep alive http in milliseconds (default: 10000)
- `connectionTimeout`: Http connection timeout to the node in milliseconds (default: 5000)
- `readTimeout`: Http read timeout to the node in milliseconds (default: 60000)
- `addTransactionRevertReason`: Enables receiving the revert reason when a transaction fails.  (default: false)
- `pollInterval`: Polling interval of the rpc request to the node (default: 10000)
- `healthcheckInterval`: Polling interval of that evenreum will use to check if the node is active (default: 10000)
- `numBlocksToWait`: Blocks to wait until we decide event is confirmed (default: 1). Overrides broadcaster config
- `numBlocksToWaitBeforeInvalidating`:  Blocks to wait until we decide event is invalidated (default: 1).  Overrides broadcaster config
- `numBlocksToWaitForMissingTx`: Blocks to wait until we decide tx is missing (default: 1)  Overrides broadcaster config

This will be an example with a complex configuration:

```yaml
ethereum:
  nodes:
  - name: default
    url: http://mainnet:8545
    pollInterval: 1000
    maxIdleConnections: 10
    keepAliveDuration: 15000
    connectionTimeout: 7000
    readTimeout: 35000
    healthcheckInterval: 3000
    addTransactionRevertReason: true
    numBlocksToWait: 1
    numBlocksToWaitBeforeInvalidating: 1
    numBlocksToWaitForMissingTx: 1
  blockStrategy: POLL

```

## Api

You can find the complete API reference in the [API documentation page](doc/api.md)

## Configuration
It can either be configured by:

1. Storing an `application.yml` next to the built JAR (copy one from `config-examples`). This overlays the defaults from `server/src/main/resources/application.yml`.
   You can specify an independent `application.yml` file giving it as parameter to the application: 
   `java -jar server/target/web3-monitoring-agent.jar --spring.config.location=file:./custom-config/`
2. Setting the associated environment variables.

| Env Variable | Default | Description |
| -------- | -------- | -------- |
| SERVER_PORT | 8060 | The port for the agent instance. |
| ETHEREUM_BLOCKSTRATEGY | POLL | The strategy for obtaining block events from an ethereum node (POLL or PUBSUB). It will be overwritten by the specific node configuration. |
| ETHEREUM_NODE_URL | http://localhost:8545 | The default ethereum node url. |
| ETHEREUM_NODE_BLOCKSTRATEGY | POLL | The strategy for obtaining block events for the ethereum node (POLL or PUBSUB).
| ETHEREUM_NODE_HEALTHCHECK_POLLINTERVAL | 2000 | The interval time in ms, in which a request is made to the ethereum node, to ensure that the node is running and functional. |
| ETHEREUM_NODE_ADD_TRANSACTION_REVERT_REASON | false | In case of a failing transaction it indicates if Eventeum should get the revert reason. Currently not working for Ganache and Parity.
| POLLING_INTERVAL | 5000 | The polling interval used by Web3j to get events from the blockchain. |
| START_FROM_BLOCK | "" | The block from where start to sync. It takes preference over the block number existing in the event store database. |
| ONLY_EVENTS_CONFIRMED | false | If is set to `true` will only process the events with CONFIRMED state.  |
| EVENTSTORE_TYPE | DB | The type of eventstore used in Eventeum. (See the Advanced section for more details) |
| BROADCASTER_TYPE | KAFKA | The broadcast mechanism to use.  (KAFKA or HTTP or RABBIT) |
| BROADCASTER_CACHE_EXPIRATIONMILLIS | 6000000 | The eventeum broadcaster has an internal cache of sent messages, which ensures that duplicate messages are not broadcast.  This is the time that a message should live within this cache. |
| BROADCASTER_EVENT_CONFIRMATION_NUMBLOCKSTOWAIT | 12 | The number of blocks to wait (after the initial mined block) before broadcasting a CONFIRMED event |
| BROADCASTER_EVENT_CONFIRMATION_NUMBLOCKSTOWAITFORMISSINGTX | 200 | After a fork, a transaction may disappear, and this is the number of blocks to wait on the new fork, before assuming that an event emitted during this transaction has been INVALIDATED |
| BROADCASTER_EVENT_CONFIRMATION_NUMBLOCKSTOWAITBEFOREINVALIDATING | 2 | Number of blocks to wait before considering a block as invalid. |
| BROADCASTER_MULTIINSTANCE | false | If multiple instances of eventeum are to be deployed in your system, this should be set to true so that the eventeum communicates added/removed filters to other instances, via kafka. |
| BROADCASTER_HTTP CONTRACTEVENTSURL | | The http url for posting contract events (for HTTP broadcasting) |
| BROADCASTER_HTTP BLOCKEVENTSURL | | The http url for posting block events (for HTTP broadcasting) |
| BROADCASTER_BYTESTOASCII | false | If any bytes values within events should be converted to ascii (default is hex) |
| BROADCASTER_ENABLE_BLOCK_NOTIFICATION | true | Boolean that indicates if want to receive block notifications or not. Set false to not receive that event. |
| ZOOKEEPER_ADDRESS | localhost:2181 | The zookeeper address |
| KAFKA_ADDRESSES | localhost:9092 | Comma seperated list of kafka addresses |
| KAFKA_TOPIC_CONTRACT_EVENTS | w3m-contract-events | The topic name for broadcast contract event messages |
| KAFKA_TOPIC_BLOCK_EVENTS | w3m-block-events | The topic name for broadcast block event messages |
| KAFKA_TOPIC_TRANSACTION_EVENTS | w3m-transaction-events | The topic name for broadcast trasaction messages |
| KAFKA_REQUEST_TIMEOUT_MS | 20000 | The duration after which a request timeouts |
| KAFKA_ENDPOINT_IDENTIFICATION_ALGORITHM | null | The endpoint identification algorithm to validate server hostname using server certificate |
| KAFKA_SASL_MECHANISM | PLAIN | The mechanism used for SASL authentication |
| KAFKA_USERNAME | "" | The username used to connect to a SASL secured Kafka cluster |
| KAFKA_PASSWORD | "" | The password used to connect to a SASL secured Kafka cluster |
| KAFKA_SECURITY_PROTOCOL | PLAINTEXT | Protocol used to communicate with Kafka brokers |
| KAFKA_RETRIES | 10 | The number of times a Kafka consumer will try to publish a message before throwing an error |
| KAFKA_RETRY_BACKOFF_MS | 500 | The duration between each retry |
| KEEP_ALIVE_DURATION | 15000 | Rpc http idle threads keep alive timeout in ms |
| MAX_IDLE_CONNECTIONS| 10 | The max number of HTTP rpc idle threads at the pool |
| SYNCINC_THRESHOLD | 60 | Number of blocks of difference to consider that eventeum is "syncing" with a node
| SPRING_DATA_MONGODB_HOST | localhost | The mongoDB host (used when event store is set to DB) |
| SPRING_DATA_MONGODB_PORT | 27017 | The mongoDB post (used when event store is set to DB) |
| RABBIT_ADDRESS | localhost:5672 | property spring.rabbitmq.host (The rabbitmq address) |
| RABBIT_EXCHANGE | ThisIsAExchange | property rabbitmq.exchange |
| RABBIT_ROUTING_KEY | thisIsRoutingKey | property rabbitmq.routingKeyPrefix |
| DATABASE_TYPE | MONGO | The database to use.  Either MONGO or SQL. |
| CONNECTION_TIMEOUT | 7000 | RPC, http connection timeout in millis |
| READ_TIMEOUT | 35000 | RPC, http read timeout in millis |

### INFURA Support Configuration
Connecting to an INFURA node is only supported if connecting via websockets (`wss://<...>` node url).  The blockstrategy must also be set to PUBSUB.

## Advanced
### Correlation Id Strategies (Kafka Broadcasting)

Each subscribed event can have a correlation id strategy association with it, during subscription.  A correlation id strategy defines what the kafka message key for a broadcast event should be, and allows the system to be configured so that events with particular parameter values are always sent to the same partition.

Currently supported correlation id strategies are:

**Indexed Parameter Strategy** - An indexed parameter within the event is used as the message key when broadcasting.
**Non Indexed Parameter Strategy** - An non-indexed parameter within the event is used as the message key when broadcasting.

### Event Store

Eventeum utilises an event store in order to establish the block number to start event subscriptions from, in the event of a failover.  For example, if the last event broadcast for event with id X had a block number of 123, then on a failover, eventeum will subscribe to events from block 124.

There are currently 2 supported event store implementations:

#### MongoDB

Broadcast events are saved and retrieved from a mongoDB database.

**Required Configuration**

| Env Variable | Default | Description |
| -------- | -------- | -------- |
| EVENTSTORE_TYPE | DB | MongoDB event store enabled |
| SPRING_DATA_MONGODB_HOST | localhost | The mongoDB host |
| SPRING_DATA_MONGODB_PORT | 27017 | The mongoDB post |

#### REST Service

Eventeum polls an external REST service in order to obtain a list of events broadcast for a specific event specification.  It is assumed that this REST service listens for broadcast events on the kafka topic and updates its internal state...broadcast events are not directly sent to the REST service by eventeum.

The implemented REST service should have a pageable endpoint which accepts a request with the following specification:

-   **URL:** Configurable, defaults to `/api/rest/v1/event`    
-   **Method:** `GET`
-   **Headers:**  

| Key | Value |
| -------- | -------- |
| content-type | application/json |

-   **URL Params:**

| Key | Value |
| -------- | -------- |
| page | The page number |
| size | The page size |
| sort | The results sort field |
| dir | The results sort direction |
| signature | Retrieve events with the specified event signature |

-   **Body:** `N/A`

-   **Success Response:**
    -   **Code:** 200  
        **Content:**

```json
{
	"content":[
		{"blockNumber":10,"id":<unique event id>}],
	"page":1,
	"size":1,
	"totalElements":1,
	"first":false,
	"last":true,
	"totalPages":1,
	"numberOfElements":1,
	"hasContent":true
}
```

**Required Configuration**

| Env Variable | Default | Description |
| -------- | -------- | -------- |
| EVENTSTORE_TYPE | REST | REST event store enabled |
| EVENTSTORE_URL  | http://localhost:8081/api/rest/v1 | The REST endpoint url |
| EVENTSTORE_EVENTPATH | /event | The path to the event REST endpoint |

## Metrics: Prometheus

Eventeum includes a prometheus metrics export endpoint.

It includes standard jvm, tomcat metrics enabled by spring-boot https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-metrics-export-prometheus https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-metrics-meter.

Added to the standard metrics, custom metrics have been added:

* eventeum_%Network%_syncing: 1 if node is syncing (latestBlock + syncingThreshols < currentBlock). 0 if not syncing
* eventeum_%Network%_latestBlock: latest block read by Eventeum
* eventeum_%Network%_currentBlock: Current node block
* eventeum_%Network%_status: Current node status. 0 = Suscribed, 1 = Connected, 2 = Down

All  metrics include application="Eventeum",environment="local" tags.

The endpoint is: GET /monitoring/prometheus


## Known Caveats / Issues
* In multi-instance mode, where there is more than one instance in a system, your services are required to handle duplicate messages gracefully, as each instance will broadcast the same events.

## Attribution

This project is based in the fantastic [Consensys Eventeum](https://github.com/consensys/eventeum) project. In Keyko we started from Eventeum 0.7 adding some additional functionalities.

## License

```
Copyright 2020 Keyko GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
