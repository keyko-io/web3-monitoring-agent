package io.keyko.monitoring.agent.kafkadl.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DummyMessage.class, name = DummyMessage.TYPE),
        @JsonSubTypes.Type(value = DummyMessageNoInterface.class, name = DummyMessageNoInterface.TYPE),
        @JsonSubTypes.Type(value = AnnotatedDummyMessage.class, name = AnnotatedDummyMessage.TYPE)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface TestMessage {
    String getId();
    String getType();
    DummyDetails getDetails();
    Integer getRetries();
}
