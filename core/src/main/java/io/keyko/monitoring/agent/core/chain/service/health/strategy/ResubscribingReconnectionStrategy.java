package io.keyko.monitoring.agent.core.chain.service.health.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.service.EventSubscriptionService;

@AllArgsConstructor
@Data
public abstract class ResubscribingReconnectionStrategy implements ReconnectionStrategy {

    private EventSubscriptionService subscriptionService;
    private BlockchainService blockchainService;

    @Override
    public void resubscribe() {
        //TODO need to figure out if we need to unregister
        subscriptionService.resubscribeToAllSubscriptions();

        blockchainService.reconnect();
    }
}
