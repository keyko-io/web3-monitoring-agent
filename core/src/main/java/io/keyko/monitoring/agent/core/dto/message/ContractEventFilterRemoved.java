package io.keyko.monitoring.agent.core.dto.message;

import lombok.NoArgsConstructor;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;

@NoArgsConstructor
public class ContractEventFilterRemoved extends AbstractMessage<ContractEventFilter> {

    public static final String TYPE = "EVENT_FILTER_REMOVED";

    public ContractEventFilterRemoved(ContractEventFilter filter) {
        super(filter.getId(), TYPE, filter);
    }
}

