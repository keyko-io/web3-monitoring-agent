package io.keyko.monitoring.agent.kafkadl.annotation;

import io.keyko.monitoring.agent.kafkadl.internal.beanregistration.KafkaDeadLetterConfiguration;
import io.keyko.monitoring.agent.kafkadl.internal.beanregistration.KafkaDeadLetterRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ KafkaDeadLetterConfiguration.class, KafkaDeadLetterRegistrar.class})
@EnableScheduling
public @interface EnableKafkaDeadLetter {
    @AliasFor("topics")
    String[] value() default {};

    @AliasFor("value")
    String[] topics() default {};

    String serviceId() default "";

    String[] containerFactoryBeans() default {"kafkaListenerContainerFactory"};
}