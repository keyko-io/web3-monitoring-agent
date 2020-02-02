package io.keyko.monitoring.agent.kafkadl.internal.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSON {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String stringify(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "<Unable to convert to JSON>";
        }
    }
}