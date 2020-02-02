package io.keyko.monitoring.agent.kafkadl.internal;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DeadLetterTopicNameConvention {

    private static final String DEAD_LETTER_TOPIC_SUFFIX = "-dlt";

    private static final String ERROR_TOPIC_SUFFIX = "-err";

    private String serviceId;

    public String getDeadLetterTopicName(String topicName) {
        return topicName + getDeadLetterTopicSuffix();
    }

    public String getErrorTopicName(String topicName) {
        return topicName + getErrorTopicSuffix();
    }

    public String getOriginalTopicFromDeadLetterTopicName(String deadLetterTopicName) {
        return deadLetterTopicName.substring(0, deadLetterTopicName.length() - getDeadLetterTopicSuffix().length());
    }

    public boolean isDeadLetterTopic(String topicName) {
        return topicName.endsWith(getDeadLetterTopicSuffix());
    }

    public String getDeadLetterTopicSuffix() {
        return DEAD_LETTER_TOPIC_SUFFIX + serviceId;
    }

    public String getErrorTopicSuffix() {
        return ERROR_TOPIC_SUFFIX + serviceId;
    }
}
