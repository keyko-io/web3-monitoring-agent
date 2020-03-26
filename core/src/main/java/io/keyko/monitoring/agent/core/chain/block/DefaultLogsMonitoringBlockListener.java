package io.keyko.monitoring.agent.core.chain.block;

import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.Web3jService;
import io.keyko.monitoring.agent.core.chain.service.container.ChainServicesContainer;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Component
@Slf4j
public class DefaultLogsMonitoringBlockListener implements BlockListener {

    private Web3jService web3jService;
    private BlockchainEventBroadcaster broadcaster;
    private Map<String, BlockchainService> blockchainServices;


    private Lock lock = new ReentrantLock();

    @Value("${fetch.all.events:false}")
    private boolean ALL_EVENTS;

    public DefaultLogsMonitoringBlockListener(ChainServicesContainer chainServicesContainer,
                                              Web3jService web3jService,
                                              BlockchainEventBroadcaster eventBroadcaster) {
        this.web3jService = web3jService;
        this.broadcaster = eventBroadcaster;

        this.blockchainServices = new HashMap<>();

        chainServicesContainer
                .getNodeNames()
                .forEach(nodeName -> {
                    blockchainServices.put(nodeName,
                            chainServicesContainer.getNodeServices(nodeName).getBlockchainService());
                });
    }

    @Override
    public void onBlock(Block block) {
        lock.lock();
        try {
            if (ALL_EVENTS)
                web3jService.broadcastAllEvents(broadcaster, blockchainServices);
        } finally {
            lock.unlock();
        }

    }
}
