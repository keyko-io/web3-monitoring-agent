package io.keyko.monitoring.agent.kafkadl.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DummyMessageNoInterface implements TestMessage {

    public static final String TYPE = "DUMMY_NO_INTERFACE";

    private String type = TYPE;

    private String id;
    private Integer retries = 0;
    private DummyDetails details;
}
