package io.keyko.monitoring.agent.core.integration.broadcast.blockchain;

import io.keyko.monitoring.agent.core.dto.view.ContractViewDetails;
import lombok.AllArgsConstructor;
import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;

@AllArgsConstructor
public class ListenerInvokingBlockchainEventBroadcaster implements BlockchainEventBroadcaster {

    private OnBlockchainEventListener listener;


    @Override
    public void broadcastNewBlock(BlockDetails block) {
        listener.onNewBlock(block);
    }

    @Override
    public void broadcastContractEvent(ContractEventDetails eventDetails) {
        listener.onContractEvent(eventDetails);
    }

    @Override
    public void broadcastContractView(ContractViewDetails viewDetails) {
        listener.onContractView(viewDetails);
    }

    @Override
    public void broadcastTransaction(TransactionDetails transactionDetails) {
        listener.onTransactionEvent(transactionDetails);
    }

    public interface OnBlockchainEventListener {

        void onNewBlock(BlockDetails block);

        void onContractEvent(ContractEventDetails eventDetails);

        void onContractView(ContractViewDetails viewDetails);

        void onTransactionEvent(TransactionDetails transactionDetails);
    }

}
