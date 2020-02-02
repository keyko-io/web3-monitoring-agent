package io.keyko.monitoring.agent.kafkadl.internal;

import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;

@AllArgsConstructor
public class KafkaProperties {

    private static final String DEFAULT_POLL_DELAY = "5000";

    private static final String DEFAULT_DEADLETTER_RETRIES = "3";

    private static final String DEFAULT_CONSUMER_RETRIES = "3";

    private static final String DEFAULT_CONSUMER_RETRY_INTERVAL = "2000";

    private Environment environment;

    public String getBootstrapAddresses() {
        return environment.getProperty("kafka.bootstrap.addresses");
    }

    public Integer getDeadLetterTopicPollDelay() {
        return Integer.parseInt(environment.getProperty("kafka.deadletter.polldelay", DEFAULT_POLL_DELAY));
    }

    public Integer getDeadLetterTopicRetries() {
        return Integer.parseInt(environment.getProperty("kafka.deadletter.retries", DEFAULT_DEADLETTER_RETRIES));
    }

    public Integer getConsumerRetries() {
        return Integer.parseInt(environment.getProperty("kafka.consumer.retry.maxAttempts", DEFAULT_CONSUMER_RETRIES));
    }

    public long getConsumerRetryInterval() {
        return Long.parseLong(environment.getProperty("kafka.consumer.retry.interval", DEFAULT_CONSUMER_RETRY_INTERVAL));
    }

    public Integer getNumTopicPartitions() {
        return Integer.parseInt(environment.getProperty("kafka.topic.numPartitions", "3"));
    }

    public Short getTopicReplicationFactor() {
        return Short.parseShort(environment.getProperty("kafka.topic.replicationFactor", "1"));
    }
}