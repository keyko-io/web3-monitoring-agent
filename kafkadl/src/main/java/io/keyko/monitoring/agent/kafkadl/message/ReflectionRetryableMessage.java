package io.keyko.monitoring.agent.kafkadl.message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionRetryableMessage implements RetryableMessage {

    private Object wrappedMessage;

    public ReflectionRetryableMessage(Object message) {
        this.wrappedMessage = message;
    }

    public static boolean isSupported(Object message) {
        try {
            getRetriesMethod(message);
            setRetriesMethod(message);
        } catch (NoSuchMethodException e) {
            return false;
        }

        return true;
    }

    @Override
    public Integer getRetries() {
        try {
            return (Integer) getRetriesMethod(wrappedMessage).invoke(wrappedMessage);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("getRetries could not be invoked on message");
        }
    }

    @Override
    public void setRetries(Integer numRetries) {
        try {
            setRetriesMethod(wrappedMessage).invoke(wrappedMessage, numRetries);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("setRetries could not be invoked on message");
        }
    }

    private static Method getRetriesMethod(Object message) throws NoSuchMethodException {
        return message.getClass().getMethod("getRetries");
    }

    private static Method setRetriesMethod(Object message) throws NoSuchMethodException {
        return message.getClass().getMethod("setRetries", Integer.class);
    }
}
