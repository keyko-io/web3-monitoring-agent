package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.dto.view.ContractViewDetails;
import org.web3j.abi.datatypes.Type;

import java.util.List;

/**
 * A factory interface for creating ContractViewDetails objects from the event filter plus the
 * Web3J.
 *
 */
public interface ContractViewDetailsFactory {
    ContractViewDetails createViewDetails(ContractViewFilter filter, List<Type> result, Block block);
}
