package io.keyko.monitoring.agent.core.dto.message;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ContractViewFilterAdded extends AbstractMessage<ContractViewFilter> {

    public static final String TYPE = "VIEW_FILTER_ADDED";

    public ContractViewFilterAdded(ContractViewFilter filter) {
        super(filter.getId(), TYPE, filter);
    }
}