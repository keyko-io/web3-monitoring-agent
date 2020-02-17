package io.keyko.monitoring.agent.core.chain.block;

import io.keyko.monitoring.agent.core.chain.service.BlockchainService;

/**
 * An abstract implementation of a block listener that can unregister itself from the system.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
public abstract class SelfUnregisteringBlockListener implements BlockListener {

    private BlockchainService blockchainService;

    protected SelfUnregisteringBlockListener(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    protected void unregister() {
        blockchainService.removeBlockListener(this);
    }
}
