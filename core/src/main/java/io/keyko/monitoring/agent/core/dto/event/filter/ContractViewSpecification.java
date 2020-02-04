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
 * @author Aitor Argomaniz <aitor@keyko.io></aitor@keyko.io>
 */
@Embeddable
@Data
@EqualsAndHashCode
public class ContractViewSpecification implements Serializable {

    private String viewName;

    @ElementCollection
    private List<ParameterDefinition> parameterDefinitions = new ArrayList<>();
}
