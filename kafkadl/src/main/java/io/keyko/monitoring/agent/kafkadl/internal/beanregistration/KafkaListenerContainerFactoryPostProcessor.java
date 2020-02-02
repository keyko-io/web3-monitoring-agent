package io.keyko.monitoring.agent.kafkadl.internal.beanregistration;

import lombok.Data;
import io.keyko.monitoring.agent.kafkadl.internal.DeadLetterSettings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.support.RetryTemplate;

@Data
public class KafkaListenerContainerFactoryPostProcessor implements BeanPostProcessor {

    private RetryTemplate retryTemplate;
    private RecoveryCallback recoveryCallback;
    private DeadLetterSettings settings;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (isEnabledFactoryBean(bean, beanName)) {
            final AbstractKafkaListenerContainerFactory factory = (AbstractKafkaListenerContainerFactory) bean;
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
            factory.getContainerProperties().setAckOnError(true);
            factory.setRetryTemplate(retryTemplate);
            factory.setRecoveryCallback(recoveryCallback);
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    protected boolean isEnabledFactoryBean(Object bean, String beanName) {
        return settings.getDeadLetterEnabledContainerFactoryBeans().contains(beanName)
                && bean instanceof AbstractKafkaListenerContainerFactory;
    }

}
