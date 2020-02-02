package io.keyko.monitoring.agent.kafkadl.internal;

import lombok.Data;
import org.springframework.core.env.Environment;

import java.util.Set;

@Data
public class DeadLetterSettings {

    private static final String ANNOTATED_MESSAGES_PROPERTY = "kafka.deadletter.annotatedMessages";

    private Set<String> deadLetterEnabledTopics;
    private String serviceId;
    private Set<String> deadLetterEnabledContainerFactoryBeans;
    private Environment environment;

    public boolean isAnnotatedMessages() {
        if (!environment.containsProperty(ANNOTATED_MESSAGES_PROPERTY)) {
            return false;
        }

        return Boolean.parseBoolean(environment.getProperty(ANNOTATED_MESSAGES_PROPERTY));
    }
}
