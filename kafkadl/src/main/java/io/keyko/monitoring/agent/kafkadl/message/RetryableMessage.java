package io.keyko.monitoring.agent.kafkadl.message;

public interface RetryableMessage {

    Integer getRetries();

    void setRetries(Integer numRetries);
}
