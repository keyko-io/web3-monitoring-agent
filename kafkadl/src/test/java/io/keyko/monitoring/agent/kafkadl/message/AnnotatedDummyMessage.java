package io.keyko.monitoring.agent.kafkadl.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.keyko.monitoring.agent.kafkadl.annotation.DeadLetterMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

@DeadLetterMessage
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnnotatedDummyMessage extends DummyMessage {

    public static final String TYPE = "ANNOTATED-DUMMY";

    @Override
    public String getType() {
        return TYPE;
    }
}
