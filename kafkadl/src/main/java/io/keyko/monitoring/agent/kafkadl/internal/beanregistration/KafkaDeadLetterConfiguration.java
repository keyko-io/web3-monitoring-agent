package io.keyko.monitoring.agent.kafkadl.internal.beanregistration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keyko.monitoring.agent.kafkadl.internal.failure.DeadLetterRecoveryCallback;
import io.keyko.monitoring.agent.kafkadl.internal.failure.SendToErrorTopicErrorHandler;
import io.keyko.monitoring.agent.kafkadl.internal.forwarder.ErrorTopicForwarder;
import io.keyko.monitoring.agent.kafkadl.internal.DeadLetterSettings;
import io.keyko.monitoring.agent.kafkadl.internal.DeadLetterTopicNameConvention;
import io.keyko.monitoring.agent.kafkadl.internal.KafkaProperties;
import io.keyko.monitoring.agent.kafkadl.handler.DeadLetterRetriesExhaustedHandler;
import io.keyko.monitoring.agent.kafkadl.internal.integration.DeadLetterTopicConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaDeadLetterConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    private DeadLetterSettings settings;

    @Bean
    public KafkaProperties kafkaProperties() {
        final KafkaProperties kafkaProperties = new KafkaProperties(environment);

        return kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, String> dltProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties().getBootstrapAddresses());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public DeadLetterTopicNameConvention deadLetterTopicNameConvention() {
        return new DeadLetterTopicNameConvention(settings.getServiceId());
    }

    @Bean
    public DeadLetterTopicConsumer deadLetterTopicConsumer(
            KafkaTemplate<?, ?> kafkaTemplate, KafkaProperties kafkaProperties) {

        return new DeadLetterTopicConsumer(dltProducerFactory(),
                deadLetterTopicConsumerErrorHandler(kafkaTemplate),
                deadLetterTopicNameConvention(),
                kafkaProperties);
    }

    @Bean
    public RetryTemplate deadLetterRetryTemplate() {
        final KafkaProperties properties = kafkaProperties();

        final RetryTemplate retryTemplate = new RetryTemplate();

        final FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(properties.getConsumerRetryInterval());
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(properties.getConsumerRetries());
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean
    public RecoveryCallback recoveryCallback(KafkaTemplate<?, ?> kafkaTemplate,
                                             DeadLetterSettings deadLetterSettings,
                                             List<DeadLetterRetriesExhaustedHandler> retriesExhaustedHandlers,
                                             ErrorTopicForwarder errorTopicForwarder) {

        return new DeadLetterRecoveryCallback(kafkaTemplate,
                deadLetterTopicNameConvention(),
                retriesExhaustedHandlers,
                errorTopicForwarder,
                kafkaProperties(),
                deadLetterSettings);
    }

    @Bean
    public ErrorHandler deadLetterTopicConsumerErrorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
        return new SendToErrorTopicErrorHandler(errorTopicForwarder(kafkaTemplate));
    }

    @Bean
    public ErrorTopicForwarder errorTopicForwarder(KafkaTemplate<?, ?> kafkaTemplate) {
        return new ErrorTopicForwarder(kafkaTemplate, deadLetterTopicNameConvention());
    }

    @Bean
    public ConsumerFactory<String, String> deadLetterTopicConsumerFactory(
            KafkaProperties properties, @Autowired(required = false) ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapAddresses());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlt");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");

        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> deadLetterTopicContainerFactory(
            KafkaProperties properties, @Autowired(required = false) ObjectMapper objectMapper) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(deadLetterTopicConsumerFactory(properties, objectMapper));
        factory.setConcurrency(1);
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

        return factory;
    }

    @Bean
    public BeanPostProcessor containerFactoryPostProcessor(RecoveryCallback recoveryCallback) {
        final KafkaListenerContainerFactoryPostProcessor postProcessor = new KafkaListenerContainerFactoryPostProcessor();

        postProcessor.setRetryTemplate(deadLetterRetryTemplate());
        postProcessor.setRecoveryCallback(recoveryCallback);
        postProcessor.setSettings(settings);

        return postProcessor;
    }

    @Bean
    public KafkaListenerConfigurer deadLetterListenerConfigurer(KafkaTemplate<?, ?> kafkaTemplate,
                                                                KafkaProperties properties,
                                                                DeadLetterSettings deadLetterSettings,
                                                                @Autowired(required = false) ObjectMapper objectMapper) {
        final DeadLetterTopicKafkaListenerConfigurer deadLetterListenerConfigurer = new DeadLetterTopicKafkaListenerConfigurer();
        deadLetterListenerConfigurer.setConsumerBean(deadLetterTopicConsumer(kafkaTemplate, kafkaProperties()));
        deadLetterListenerConfigurer.setContainerFactory(deadLetterTopicContainerFactory(properties, objectMapper));
        deadLetterListenerConfigurer.setDltNameConvention(deadLetterTopicNameConvention());
        deadLetterListenerConfigurer.setDeadLetterSettings(deadLetterSettings);

        return deadLetterListenerConfigurer;
    }
}
