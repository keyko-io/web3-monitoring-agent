package io.keyko.monitoring.agent.kafkadl.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DummyMessage implements RetryableMessage, TestMessage {

    public static final String TYPE = "DUMMY";

    private String id;
    private String type = TYPE;
    private DummyDetails details;
    private Integer retries = 0;
}
