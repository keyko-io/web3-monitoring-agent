package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.chain.factory.BlockDetailsFactory;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A contract event listener that saves the ContractEventDetails to a SaveableEventStore.
 *
 * Only gets registered if a SaveableEventStore exists in the context.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class EventStoreLatestBlockUpdater implements BlockListener {

    private SaveableEventStore saveableEventStore;

    private BlockDetailsFactory blockDetailsFactory;

    @Autowired
    public EventStoreLatestBlockUpdater(
            SaveableEventStore saveableEventStore, BlockDetailsFactory blockDetailsFactory) {
        this.saveableEventStore = saveableEventStore;
        this.blockDetailsFactory = blockDetailsFactory;
    }

    @Override
    public void onBlock(Block block) {
        saveableEventStore.save(new LatestBlock(blockDetailsFactory.createBlockDetails(block)));
    }
}
