package io.keyko.monitoring.agent.kafkadl.internal.beanregistration;

import io.keyko.monitoring.agent.kafkadl.annotation.EnableKafkaDeadLetter;
import io.keyko.monitoring.agent.kafkadl.internal.DeadLetterSettings;
import io.keyko.monitoring.agent.kafkadl.internal.DeadLetterTopicNameConvention;
import io.keyko.monitoring.agent.kafkadl.internal.beanregistration.exception.ServiceIdNotConsistentException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KafkaDeadLetterRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware, EnvironmentAware {

    public static final String DEAD_LETTER_SETTINGS_BEAN_NAME = "deadLetterSettings";

    private ClassLoader beanClassloader;

    private Environment environment;

    private String serviceId;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassloader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        for (String beanName : registry.getBeanDefinitionNames()) {
            final BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);

            if (beanDefinition instanceof AnnotatedBeanDefinition) {
                final AnnotatedBeanDefinition potentialDeadLetterEnabled = (AnnotatedBeanDefinition) beanDefinition;

                final Class<?> beanClass = getClassForBean(potentialDeadLetterEnabled);

                if (beanClass != null && isDeadLetterEnableAnnotatedClass(beanClass)) {
                    String annotationServiceId = getServiceId(beanClass);

                    //Service id must be the same throughout the service
                    if (serviceId != null && !serviceId.equals(annotationServiceId)) {
                        throw new ServiceIdNotConsistentException(serviceId, annotationServiceId);
                    }

                    serviceId = annotationServiceId;

                    registerDeadLetterBeans(getDeadLetterEnabledTopics(beanClass),
                            getDeadLetterEnabledContainerFactories(beanClass), registry);
                }
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private void registerDeadLetterBeans(
            Set<String> topics, Set<String> containerFactoryBeans, BeanDefinitionRegistry registry) {
        registerDeadLetterSettings(topics, containerFactoryBeans, registry);

        topics.forEach((topicName) -> {
            registerTopicBean(topicName, null, registry);
            registerDeadLetterTopicBean(topicName, registry);
            registerErrorTopicBean(topicName, registry);
        });
    }

    private void registerDeadLetterSettings(
            Set<String> topics, Set<String> containerFactoryBeans, BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                DeadLetterSettings.class);
        builder.addPropertyValue("deadLetterEnabledTopics", topics);
        builder.addPropertyValue("serviceId", serviceId);
        builder.addPropertyValue("deadLetterEnabledContainerFactoryBeans", containerFactoryBeans);
        builder.addPropertyValue("environment", environment);

        registry.registerBeanDefinition(DEAD_LETTER_SETTINGS_BEAN_NAME, builder.getBeanDefinition());
    }

    private void registerDeadLetterTopicBean(String topicName, BeanDefinitionRegistry registry) {
        registerTopicBean(topicName,
                getDeadLetterTopicNameConvention().getDeadLetterTopicSuffix(), registry);
    }

    private void registerErrorTopicBean(String topicName, BeanDefinitionRegistry registry) {
        registerTopicBean(topicName,
                getDeadLetterTopicNameConvention().getErrorTopicSuffix(), registry);
    }

    private void registerTopicBean(String topicName, String topicSuffix, BeanDefinitionRegistry registry) {
        String beanName = topicName;
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TopicBeanFactory.class);
        builder.addPropertyValue("topicName", topicName);
        builder.addPropertyReference("kafkaProperties", "kafkaProperties");
        if (topicSuffix != null) {
            builder.addPropertyValue("topicSuffix", topicSuffix);
            beanName = beanName + topicSuffix;
        }

        final BeanDefinition definition = builder.getBeanDefinition();
        registry.registerBeanDefinition(beanName, definition);
    }

    private Class<?> getClassForBean(AnnotatedBeanDefinition beanDefinition) {
        try {
            if (beanDefinition.getBeanClassName() == null) {
                return null;
            }
            return ClassUtils.forName(beanDefinition.getBeanClassName(), beanClassloader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Can't find the bean class...this should not happen!", e);
        }
    }

    private boolean isDeadLetterEnableAnnotatedClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(EnableKafkaDeadLetter.class);
    }

    private Set<String> getDeadLetterEnabledTopics(Class<?> clazz) {
        final Set<String> topics = new HashSet<>();
        EnableKafkaDeadLetter[] annotations = clazz.getAnnotationsByType(EnableKafkaDeadLetter.class);

        for (EnableKafkaDeadLetter annotation : annotations) {
            topics.addAll(Arrays.asList(annotation.topics()));
        }

        return topics;
    }

    private Set<String> getDeadLetterEnabledContainerFactories(Class<?> clazz) {
        final Set<String> factories = new HashSet<>();
        EnableKafkaDeadLetter[] annotations = clazz.getAnnotationsByType(EnableKafkaDeadLetter.class);

        for (EnableKafkaDeadLetter annotation : annotations) {
            factories.addAll(Arrays.asList(annotation.containerFactoryBeans()));
        }

        return factories;
    }

    private String getServiceId(Class<?> clazz) {
        EnableKafkaDeadLetter[] annotations = clazz.getAnnotationsByType(EnableKafkaDeadLetter.class);

        String serviceId = null;

        for (EnableKafkaDeadLetter annotation : annotations) {
            String annotationServiceId = annotation.serviceId();

            //Service id must be the same throughout the service
            if (serviceId != null && !serviceId.equals(annotationServiceId)) {
                throw new ServiceIdNotConsistentException(serviceId, annotationServiceId);
            }

            serviceId = annotationServiceId;
        }

        return serviceId;
    }

    protected DeadLetterTopicNameConvention getDeadLetterTopicNameConvention() {
        return new DeadLetterTopicNameConvention(serviceId);
    }
}
