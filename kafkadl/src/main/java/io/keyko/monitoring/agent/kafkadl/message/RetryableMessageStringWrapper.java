package io.keyko.monitoring.agent.kafkadl.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

public class RetryableMessageStringWrapper implements RetryableMessage {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String fullMessageString;

    private RetryableMessage retryableMessage;

    public RetryableMessageStringWrapper(String fullMessageString) {
        this.fullMessageString = fullMessageString;

        try {
            retryableMessage = OBJECT_MAPPER.readValue(fullMessageString, RetryableMessageImpl.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Message string is not a retryable message representation!", e);
        }
    }

    public String toString() {
        return fullMessageString;
    }

    @Override
    public Integer getRetries() {
        return retryableMessage.getRetries();
    }

    @Override
    public void setRetries(Integer numRetries) {
        retryableMessage.setRetries(numRetries);
        updateMessageStringRetries(numRetries);
    }

    public static boolean isRetryableMessage(String messageString) {
        try {
            OBJECT_MAPPER.readValue(messageString, RetryableMessage.class);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    //TODO Do this more elegantly!
    private void updateMessageStringRetries(Integer numRetries) {
        final String[] valuesSplit = fullMessageString.split(",");

        for (int i = 0; i < valuesSplit.length; i++) {
            final String value = valuesSplit[i];
            if (value.contains("retries")) {
                fullMessageString = fullMessageString.replace(value, "\"retries\":" + numRetries);
                if (i == valuesSplit.length - 1) {
                    fullMessageString = fullMessageString + "}";
                }
            }
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RetryableMessageImpl implements RetryableMessage {
        private Integer retries;
    }
}
