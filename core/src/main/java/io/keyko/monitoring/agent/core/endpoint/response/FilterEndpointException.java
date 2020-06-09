package io.keyko.monitoring.agent.core.endpoint.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error processing the filter")
public class FilterEndpointException extends RuntimeException {

    public FilterEndpointException(String message) {
        super(message);
    }
}
