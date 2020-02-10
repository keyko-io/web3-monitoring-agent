package io.keyko.monitoring.agent.core.chain.service.health.strategy;

import io.keyko.monitoring.agent.core.service.ViewSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.service.EventSubscriptionService;

/**
 * An NodeFailureListener that reconnects the blockchain service and resubscribes to all
 * active event subscriptions on recovery.
 * <p>
 * Note:  All subscriptions are unregistered before being reregistered.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
public class HttpReconnectionStrategy extends ResubscribingReconnectionStrategy {

    public HttpReconnectionStrategy(EventSubscriptionService subscriptionService, ViewSubscriptionService viewSubscriptionService, BlockchainService blockchainService) {
        super(subscriptionService, viewSubscriptionService, blockchainService);
    }

    @Override
    public void reconnect() {
        //Do Nothing
    }
}
