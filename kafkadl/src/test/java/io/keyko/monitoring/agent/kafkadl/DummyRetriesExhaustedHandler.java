package io.keyko.monitoring.agent.kafkadl;

import lombok.Getter;
import io.keyko.monitoring.agent.kafkadl.handler.DeadLetterRetriesExhaustedHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.retry.RetryContext;

import java.util.ArrayList;
import java.util.List;

public class DummyRetriesExhaustedHandler implements DeadLetterRetriesExhaustedHandler {

    @Getter
    List<ConsumerRecord> failedRecords = new ArrayList<>();

    @Override
    public void onFailure(RetryContext context) {
        failedRecords.add((ConsumerRecord) context.getAttribute("record"));
    }
}
