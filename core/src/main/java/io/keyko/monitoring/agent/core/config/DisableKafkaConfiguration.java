package io.keyko.monitoring.agent.core.config;

import io.keyko.monitoring.agent.core.annotation.ConditionalOnKafkaRequired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Disable Spring Boot Kafka auto configuration if its not needed.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@Configuration
@ConditionalOnKafkaRequired(false)
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class})
public class DisableKafkaConfiguration {
}
