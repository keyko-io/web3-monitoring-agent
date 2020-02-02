package io.keyko.monitoring.agent.core.server.config;

import io.keyko.monitoring.agent.core.annotation.ConditionalOnKafkaRequired;
import io.keyko.monitoring.agent.kafkadl.annotation.EnableKafkaDeadLetter;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKafkaDeadLetter(topics = {"#{eventeumKafkaSettings.eventeumEventsTopic}"},
                       containerFactoryBeans = {"kafkaListenerContainerFactory", "eventeumKafkaListenerContainerFactory"},
                       serviceId = "eventeum")
@ConditionalOnKafkaRequired
public class EventeumServerConfiguration {
}
