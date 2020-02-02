package io.keyko.monitoring.agent.kafkadl.internal.integration;

import lombok.AllArgsConstructor;
import io.keyko.monitoring.agent.kafkadl.internal.KafkaProperties;
import io.keyko.monitoring.agent.kafkadl.internal.util.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ErrorHandler;

import java.util.List;

@AllArgsConstructor
public abstract class BatchContinueOnFailureConsumer<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(BatchMessageListener.class);

    private KafkaProperties kafkaProperties;
    private ErrorHandler errorHandler;

    public void onMessages(List<ConsumerRecord<K, V>> data) {
        try {
            Thread.sleep(kafkaProperties.getDeadLetterTopicPollDelay());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        data.forEach((record) -> {
            try {
                onMessage(record);
            } catch (Exception e) {
                LOG.error(String.format("There was an error whilst processing message: %s, from topic: %s",
                        JSON.stringify(record.value()), record.topic()), e);

                errorHandler.handle(e, record);
            }
        });
    }

    abstract void onMessage(ConsumerRecord<K, V> consumerRecord);
}
