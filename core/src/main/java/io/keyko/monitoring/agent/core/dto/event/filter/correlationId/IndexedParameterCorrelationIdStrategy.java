package io.keyko.monitoring.agent.core.dto.event.filter.correlationId;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import lombok.NoArgsConstructor;

/**
 * A CorrelationIdStrategy that considers the correlation id of a specific contract event
 * to be the value of an indexed parameter at a specified index.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@NoArgsConstructor
public class IndexedParameterCorrelationIdStrategy extends ParameterCorrelationIdStrategy {

    public static final String TYPE = "INDEXED_PARAMETER";

    public IndexedParameterCorrelationIdStrategy(int parameterIndex) {
        super(TYPE, parameterIndex);
    }

    @Override
    public String getCorrelationId(ContractEventDetails contractEvent) {
        return contractEvent
                .getIndexedParameters()
                .get(getParameterIndex())
                .getValueString();
    }
}