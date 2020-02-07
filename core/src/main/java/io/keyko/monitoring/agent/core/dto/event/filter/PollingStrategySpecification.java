package io.keyko.monitoring.agent.core.dto.event.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the polling strategy used for querying the state in Smart Contracts.
 *
 */
@Embeddable
@Data
@EqualsAndHashCode
public class PollingStrategySpecification implements Serializable {

    Integer blockInterval;

}
