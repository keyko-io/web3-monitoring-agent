package io.keyko.monitoring.agent.kafkadl;

import io.keyko.monitoring.agent.kafkadl.annotation.EnableKafkaDeadLetter;
import io.keyko.monitoring.agent.kafkadl.message.TestMessage;
import io.keyko.monitoring.agent.kafkadl.handler.DeadLetterRetriesExhaustedHandler;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaDeadLetter(topics = {"testTopic"}, serviceId = "TestApplication")
public class TestConfiguration {

    @Value("${kafka.bootstrap.addresses}")
    private String bootstrapAddresses;

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddresses);
        return new KafkaAdmin(configs);
    }

    @Bean
    public ConsumerFactory<String, TestMessage> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddresses);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(props,
                null, new JsonDeserializer<>(TestMessage.class));
    }

    @Bean
    public ProducerFactory<String, TestMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddresses);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, TestMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TestMessage> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, TestMessage> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public DeadLetterRetriesExhaustedHandler customRetriesExhaustedHandler() {
        return new DummyRetriesExhaustedHandler();
    }
}
