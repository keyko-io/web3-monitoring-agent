package io.keyko.monitoring.agent.core.dto.message;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ContractViewFilterRemoved extends AbstractMessage<ContractViewFilter> {

    public static final String TYPE = "VIEW_FILTER_REMOVED";

    public ContractViewFilterRemoved(ContractViewFilter filter) {
        super(filter.getId(), TYPE, filter);
    }
}

