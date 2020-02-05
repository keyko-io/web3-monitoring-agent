package io.keyko.monitoring.agent.core.dto.view.parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.keyko.monitoring.agent.core.dto.parameter.ParameterTypeIdResolver;

import java.io.Serializable;

/**
 * A parameter related with a view.
 *
 * @param <T> The java type that represents the value of the view parameter.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonTypeIdResolver(ParameterTypeIdResolver.class)
public interface ViewParameter<T extends Serializable> extends Serializable {
    String getType();

    T getValue();

    String getName();

    @JsonIgnore
    String getValueString();
}
