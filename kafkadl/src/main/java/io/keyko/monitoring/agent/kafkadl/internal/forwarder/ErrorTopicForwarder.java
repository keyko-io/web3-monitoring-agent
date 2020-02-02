package io.keyko.monitoring.agent.kafkadl.internal.forwarder;

import lombok.AllArgsConstructor;
import io.keyko.monitoring.agent.kafkadl.internal.DeadLetterTopicNameConvention;
import io.keyko.monitoring.agent.kafkadl.internal.util.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@AllArgsConstructor
public class ErrorTopicForwarder implements RecordForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorTopicForwarder.class);

    private KafkaTemplate kafkaTemplate;
    private DeadLetterTopicNameConvention dltNameConvention;

    @Override
    public void forward(ConsumerRecord record) {
        String errorTopic = null;
        try {
            errorTopic = getErrorTopic(record);
            LOG.info(String.format("Sending record to error topic: %s", errorTopic));

            kafkaTemplate.send(errorTopic, record.key(), record.value());
        } catch (Throwable t) {
            LOG.error(String.format(
                    "There was an error when attempting to send a message to error topic %s. Message: %s",
                    errorTopic, JSON.stringify(record.value())));
        }
    }

    private String getErrorTopic(ConsumerRecord record) {
        String originalTopic = record.topic();

        if (dltNameConvention.isDeadLetterTopic(originalTopic)) {
            originalTopic = dltNameConvention.getOriginalTopicFromDeadLetterTopicName(originalTopic);
        }

        return dltNameConvention.getErrorTopicName(originalTopic);
    }
}
