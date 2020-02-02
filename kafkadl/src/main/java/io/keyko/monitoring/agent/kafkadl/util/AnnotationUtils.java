package io.keyko.monitoring.agent.kafkadl.util;

import io.keyko.monitoring.agent.kafkadl.annotation.DeadLetterMessage;

public class AnnotationUtils {

    public static boolean isDeadLetterMessageAnnotated(Object object) {
        return object.getClass().isAnnotationPresent(DeadLetterMessage.class);
    }
}
