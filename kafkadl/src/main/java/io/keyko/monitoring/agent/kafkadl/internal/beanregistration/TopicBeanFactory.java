package io.keyko.monitoring.agent.kafkadl.internal.beanregistration;

import lombok.Data;
import io.keyko.monitoring.agent.kafkadl.internal.KafkaProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.FactoryBean;

@Data
public class TopicBeanFactory implements FactoryBean<NewTopic> {

    private String topicName;

    private String topicSuffix;

    private KafkaProperties kafkaProperties;

    public TopicBeanFactory() {
    }

    @Override
    public NewTopic getObject() throws Exception {
        return new NewTopic(getFullTopicName(),
                kafkaProperties.getNumTopicPartitions(), kafkaProperties.getTopicReplicationFactor());
    }

    @Override
    public Class<?> getObjectType() {
        return NewTopic.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private String getFullTopicName() {
        return getTopicName() + getTopicSuffix();
    }
}
