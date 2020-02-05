package io.keyko.monitoring.agent.core.dto.parameter;

import java.io.Serializable;

import javax.persistence.Embeddable;

import io.keyko.monitoring.agent.core.dto.event.parameter.EventParameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public abstract class AbstractParameter<T extends Serializable> implements EventParameter<T> {

    private String type;

    private T value;

    private String name;
}
