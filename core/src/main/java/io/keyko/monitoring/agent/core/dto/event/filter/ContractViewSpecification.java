package io.keyko.monitoring.agent.core.dto.event.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents contract view specification, to be used when registering a new filter.
 *
 */
@Embeddable
@Data
@EqualsAndHashCode
public class ContractViewSpecification implements Serializable {

    private String methodName;

    @ElementCollection
    private List<MethodParameterDefinition> inputParameterDefinitions = new ArrayList<>();

    @ElementCollection
    private List<ParameterDefinition> outputParameterDefinitions = new ArrayList<>();
}
