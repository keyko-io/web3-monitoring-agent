package io.keyko.monitoring.agent.core.dto.message;

import io.keyko.monitoring.agent.core.dto.view.ContractViewDetails;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ContractView extends AbstractMessage<ContractViewDetails> {

    public static final String TYPE = "CONTRACT_VIEW";

    public ContractView(ContractViewDetails details) {
        super(details.getId(), TYPE, details);
    }
}
