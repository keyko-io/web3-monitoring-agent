package io.keyko.monitoring.agent.core.chain.service.health.strategy;

import io.keyko.monitoring.agent.core.service.SubscriptionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;

@AllArgsConstructor
@Data
public abstract class ResubscribingReconnectionStrategy implements ReconnectionStrategy {

    private SubscriptionService subscriptionService;
    private BlockchainService blockchainService;

    @Override
    public void resubscribe() {
        //TODO need to figure out if we need to unregister
        subscriptionService.resubscribeToAllSubscriptions();

        blockchainService.reconnect();
    }
}
