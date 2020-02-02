package io.keyko.monitoring.agent.kafkadl.internal.beanregistration.exception;

public class ServiceIdNotConsistentException extends RuntimeException {

    public ServiceIdNotConsistentException(String serviceId, String otherServiceId) {
        super(String.format("Service id's don't match: %s %s", serviceId, otherServiceId));
    }
}
