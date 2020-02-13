package io.keyko.monitoring.agent.core.chain.service.health.strategy;

import io.keyko.monitoring.agent.core.service.ViewSubscriptionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.service.EventSubscriptionService;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Data
@Slf4j
public abstract class ResubscribingReconnectionStrategy implements ReconnectionStrategy {

    private EventSubscriptionService subscriptionService;
    private ViewSubscriptionService viewSubscriptionService;
    private BlockchainService blockchainService;

    @Override
    public void resubscribe() {
        //TODO need to figure out if we need to unregister
        log.info("Re-subscribing to existing subscriptions");
        subscriptionService.resubscribeToAllSubscriptions();
        viewSubscriptionService.resubscribeToAllSubscriptions();

        blockchainService.reconnect();
    }
}
