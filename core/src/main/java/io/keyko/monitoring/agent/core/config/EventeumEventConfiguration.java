package io.keyko.monitoring.agent.core.config;

import io.keyko.monitoring.agent.core.integration.KafkaSettings;
import io.keyko.monitoring.agent.core.integration.broadcast.internal.DoNothingEventeumMessageBroadcaster;
import io.keyko.monitoring.agent.core.integration.broadcast.internal.EventeumMessageBroadcaster;
import io.keyko.monitoring.agent.core.integration.broadcast.internal.KafkaEventeumMessageBroadcaster;
import io.keyko.monitoring.agent.core.integration.consumer.EventeumInternalEventConsumer;
import io.keyko.monitoring.agent.core.integration.consumer.KafkaFilterEventConsumer;
import io.keyko.monitoring.agent.core.service.events.SubscriptionService;
import io.keyko.monitoring.agent.core.service.transactions.TransactionMonitoringService;
import org.apache.avro.generic.GenericRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Spring bean configuration for the FilterEvent broadcaster and consumer.
 * <p>
 * If broadcaster.multiInstance is set to true, then register a Kafka broadcaster,
 * otherwise register a dummy broadcaster that does nothing.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
public class EventeumEventConfiguration {

    @Bean
    @ConditionalOnProperty(name = "broadcaster.multiInstance", havingValue = "true")
    public EventeumMessageBroadcaster kafkaFilterEventBroadcaster(KafkaTemplate<String, GenericRecord> kafkaTemplate,
                                                                  KafkaSettings kafkaSettings) {
        return new KafkaEventeumMessageBroadcaster(kafkaTemplate, kafkaSettings);
    }

    @Bean
    @ConditionalOnProperty(name = "broadcaster.multiInstance", havingValue = "true")
    public EventeumInternalEventConsumer kafkaFilterEventConsumer(SubscriptionService subscriptionService,
                                                                  TransactionMonitoringService transactionMonitoringService,
                                                                  KafkaSettings kafkaSettings) {
        return new KafkaFilterEventConsumer(subscriptionService, transactionMonitoringService, kafkaSettings);
    }

    @Bean
    @ConditionalOnProperty(name = "broadcaster.multiInstance", havingValue = "false")
    public EventeumMessageBroadcaster doNothingFilterEventBroadcaster() {
        return new DoNothingEventeumMessageBroadcaster();
    }
}
