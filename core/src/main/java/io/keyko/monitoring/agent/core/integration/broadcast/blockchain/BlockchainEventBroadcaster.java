package io.keyko.monitoring.agent.core.integration.broadcast.blockchain;

import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.view.ContractViewDetails;

/**
 * An interface for a class that broadcasts ethereum blockchain details to the wider system.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
public interface BlockchainEventBroadcaster {

    /**
     * Broadcast details of a new block that has been mined.
     *
     * @param block block to broadcast
     */
    void broadcastNewBlock(BlockDetails block);

    /**
     * Broadcasts details of a new smart contract event that has been emitted from the ethereum blockchain.
     * @param eventDetails event to broadcast
     */
    void broadcastContractEvent(ContractEventDetails eventDetails);

    /**
     * Broadcasts details of a new smart contract view that has been emitted from the ethereum blockchain.
     * @param viewDetails view to broadcast
     */
    void broadcastContractView(ContractViewDetails viewDetails);

    /**
     * Broadcasts details of a monitored transaction that has been mined.
     * @param transactionDetails transaction to broadcast
     */
    void broadcastTransaction(TransactionDetails transactionDetails);
}
