package io.keyko.monitoring.agent.kafkadl.internal.beanregistration;

import lombok.Data;
import io.keyko.monitoring.agent.kafkadl.internal.DeadLetterSettings;
import io.keyko.monitoring.agent.kafkadl.internal.DeadLetterTopicNameConvention;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.lang.reflect.Method;
import java.util.UUID;

@Data
public class DeadLetterTopicKafkaListenerConfigurer implements KafkaListenerConfigurer, BeanFactoryAware {

    private Object consumerBean;
    private DeadLetterSettings deadLetterSettings;
    private KafkaListenerContainerFactory containerFactory;
    private DeadLetterTopicNameConvention dltNameConvention;

    private BeanFactory beanFactory;

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {

        for (String topicName : deadLetterSettings.getDeadLetterEnabledTopics()) {
            final MethodKafkaListenerEndpoint<String, Object> endpoint = new MethodKafkaListenerEndpoint<String, Object>();
            endpoint.setMethod(getConsumerMethod());
            endpoint.setBeanFactory(beanFactory);
            endpoint.setBean(consumerBean);
            endpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());
            endpoint.setId(UUID.randomUUID().toString());
            endpoint.setGroupId("dlt");
            endpoint.setTopics(dltNameConvention.getDeadLetterTopicName(parseExpression(topicName)));

            registrar.registerEndpoint(endpoint, containerFactory);
        }
    }

    private String parseExpression(String expressionString) {
        if (expressionString.startsWith("#{") && expressionString.endsWith("}")) {
            final ExpressionParser expressionParser = new SpelExpressionParser();
            //TODO Look into making this better!
            expressionString = expressionString.replace("{", "");
            expressionString = expressionString.replace("}", "");
            expressionString = expressionString.replace("#", "@");

            final StandardEvaluationContext context = new StandardEvaluationContext();
            context.setBeanResolver(new BeanFactoryResolver(beanFactory));

            final Expression expression = expressionParser.parseExpression(expressionString);
            return expression.getValue(context, String.class);
        }

        return expressionString;
    }

    private Method getConsumerMethod() {
        return getMethodWithName(consumerBean.getClass(), "onMessages");
    }

    private Method getMethodWithName(Class clazz, String name) {
        for(Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
