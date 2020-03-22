package io.keyko.monitoring.agent.core.chain.service.health;

import io.keyko.monitoring.agent.core.chain.service.health.strategy.ReconnectionStrategy;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import io.keyko.monitoring.agent.core.service.ViewSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.monitoring.EventeumValueMonitor;
import io.keyko.monitoring.agent.core.service.EventStoreService;
import io.keyko.monitoring.agent.core.service.EventSubscriptionService;

import java.math.BigInteger;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A service that constantly polls an ethereum node (getCurrentBlockNumber) in order to ensure that the node
 * is currently running.  If a failure is detected, each configured NodeFailureListener is invoked.
 * This is also the case when it is detected that a node has recovered after failure.
 * <p>
 * The poll interval can be configured with the ethereum.node.healthcheck.pollInterval property.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@Slf4j
public class NodeHealthCheckService {

    private BlockchainService blockchainService;

    private NodeStatus nodeStatus;

    private ReconnectionStrategy reconnectionStrategy;

    private EventSubscriptionService eventSubscriptionService;

    private ViewSubscriptionService viewSubscriptionService;

    private boolean initiallySubscribed = false;

    private AtomicLong currentBlock;

    private AtomicInteger syncing;

    private AtomicInteger nodeStatusGauge;

    private EventStoreService eventStoreService;

    private Integer syncingThreshold;

    public NodeHealthCheckService(BlockchainService blockchainService,
                                  ReconnectionStrategy reconnectionStrategy,
                                  EventSubscriptionService eventSubscriptionService,
                                  ViewSubscriptionService viewSubscriptionService,
                                  EventeumValueMonitor valueMonitor,
                                  EventStoreService eventStoreService,
                                  Integer syncingThreshold,
                                  ScheduledThreadPoolExecutor taskScheduler,
                                  Long healthCheckPollInterval) {
        this.eventStoreService = eventStoreService;
        this.blockchainService = blockchainService;
        this.reconnectionStrategy = reconnectionStrategy;
        this.eventSubscriptionService = eventSubscriptionService;
        this.viewSubscriptionService = viewSubscriptionService;
        this.syncingThreshold = syncingThreshold;
        nodeStatus = NodeStatus.SUBSCRIBED;

        currentBlock = valueMonitor.monitor("currentBlock", blockchainService.getNodeName(), new
                AtomicLong(0));
        nodeStatusGauge = valueMonitor.monitor("status", blockchainService.getNodeName(), new
                AtomicInteger(NodeStatus.SUBSCRIBED.ordinal()));
        syncing = valueMonitor.monitor("syncing", blockchainService.getNodeName(), new
                AtomicInteger(0));

        taskScheduler.scheduleWithFixedDelay(() -> this.checkHealth(), 0, healthCheckPollInterval, TimeUnit.MILLISECONDS);
    }

    public void checkHealth() {
        try {
            log.trace("Checking health");

            final NodeStatus statusAtStart = nodeStatus;

            //Can take a few seconds to subscribe initially so if wait until after
            //first subscription to check health
            if (!isSubscribed() && !initiallySubscribed) {
                log.debug("Not initially subscribed");
                return;
            }
            if (isNodeConnected()) {
                log.trace("Node connected");

                if (nodeStatus == NodeStatus.DOWN) {
                    log.info("Node {} has come back up.", blockchainService.getNodeName());
                    //We've come back up
                    doResubscribe();
                }   else if (eventSubscriptionService.getFilterSubscriptions().size() !=
                        eventSubscriptionService.listContractEventFilters().size()) {
                    eventSubscriptionService.resubscribeToAllSubscriptions();
                }   else if (viewSubscriptionService.getFilterSubscriptions().size() !=
                        viewSubscriptionService.listContractViewFilters().size()) {
                    viewSubscriptionService.resubscribeToAllSubscriptions();
                }

            } else {
                log.error("Node {} is down!!", blockchainService.getNodeName());
                nodeStatus = NodeStatus.DOWN;

                if (statusAtStart != NodeStatus.DOWN) {
                    eventSubscriptionService.unsubscribeToAllSubscriptions(blockchainService.getNodeName());
                    blockchainService.disconnect();
                }

                doReconnect();
            }

            nodeStatusGauge.set(nodeStatus.ordinal());
        } catch (Throwable t) {
            log.error("An error occured during the check health / recovery process...Will retry at next poll", t);
        }
    }

    protected boolean isNodeConnected() {
        try {
            currentBlock.set(blockchainService.getCurrentBlockNumber().longValue());

            if (currentBlock.longValue() <= syncingThreshold + getLatestBlockForNode().getNumber().longValue()) {
                syncing.set(0);
            } else {
                syncing.set(1);
            }
        } catch (Throwable t) {
            log.error("Get latest block failed with exception on node " + blockchainService.getNodeName(), t);

            return false;
        }

        return true;
    }

    protected boolean isSubscribed() {
        return blockchainService.isConnected() &&
                eventSubscriptionService.isFullySubscribed(blockchainService.getNodeName());
    }

    private void doReconnect() {
        reconnectionStrategy.reconnect();

        if (isNodeConnected()) {
            nodeStatus = NodeStatus.CONNECTED;
            doResubscribe();
        }
    }

    private void doResubscribe() {
        reconnectionStrategy.resubscribe();

        nodeStatus = isSubscribed() ? NodeStatus.CONNECTED : NodeStatus.DOWN;
    }

    private LatestBlock getLatestBlockForNode() {
        return eventStoreService.getLatestBlock(
                blockchainService.getNodeName()).orElseGet(() -> {
            final LatestBlock latestBlock = new LatestBlock();
            latestBlock.setNumber(BigInteger.ZERO);
            return latestBlock;
        });
    }

    private enum NodeStatus {
        CONNECTED,
        SUBSCRIBED,
        DOWN
    }

}
