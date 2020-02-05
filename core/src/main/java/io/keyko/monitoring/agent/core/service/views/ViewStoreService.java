package io.keyko.monitoring.agent.core.service.views;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.model.LatestBlock;

import java.util.Optional;

/**
 * A service that interacts with the view store in order to retrieve data required by Eventeum.
 *
 */
public interface ViewStoreService {

    /**
     * Returns the contract view with the latest block, that matches the view signature.
     *
     * @param viewSignature  The view signature
     * @param contractAddress The view contract address
     * @return The view details
     */
    Optional<ContractEventDetails> getLatestContractEvent(String viewSignature, String contractAddress);

    /**
     * Returns the latest block, for the specified node.
     *
     * @param nodeName The nodename
     * @return The block details
     */
    Optional<LatestBlock> getLatestBlock(String nodeName);
}
