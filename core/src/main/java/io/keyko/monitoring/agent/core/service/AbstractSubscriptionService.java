package io.keyko.monitoring.agent.core.service;

import io.keyko.monitoring.agent.core.chain.block.BlockListener;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.model.FilterSubscription;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSubscriptionService {

    protected Map<String, FilterSubscription> filterSubscriptions = new ConcurrentHashMap<>();

    protected void subscribeToNewBlockEvents(
            BlockchainService blockchainService, List<BlockListener> blockListeners) {
        blockListeners.forEach(listener -> blockchainService.addBlockListener(listener));

        blockchainService.connect();
    }

    protected FilterSubscription getFilterSubscription(String filterId) {
        return filterSubscriptions.get(filterId);
    }

}
