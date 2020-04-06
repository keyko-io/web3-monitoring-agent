package io.keyko.monitoring.agent.core.chain.block.tx;

import io.keyko.monitoring.agent.core.chain.block.tx.criteria.TransactionMatchingCriteria;
import io.keyko.monitoring.agent.core.chain.service.container.ChainServicesContainer;
import io.keyko.monitoring.agent.core.chain.service.domain.Transaction;
import io.keyko.monitoring.agent.core.chain.service.domain.TransactionReceipt;
import io.keyko.monitoring.agent.core.dto.log.LogDetails;
import io.keyko.monitoring.agent.core.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import lombok.extern.slf4j.Slf4j;
import io.keyko.monitoring.agent.core.chain.factory.TransactionDetailsFactory;
import io.keyko.monitoring.agent.core.chain.service.BlockCache;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.chain.settings.Node;
import io.keyko.monitoring.agent.core.chain.settings.NodeSettings;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class DefaultTransactionMonitoringBlockListener implements TransactionMonitoringBlockListener {

    //Keyed by node name
    private Map<String, List<TransactionMatchingCriteria>> criteria;

    //Keyed by node name
    private Map<String, BlockchainService> blockchainServices;

    private BlockchainEventBroadcaster broadcaster;

    private TransactionDetailsFactory transactionDetailsFactory;

    private BlockCache blockCache;

    private RetryTemplate retryTemplate;

    private Lock lock = new ReentrantLock();

    private NodeSettings nodeSettings;

    @Value("${fetch.all.transactions:false}")
    private boolean ALL_TRANSACTIONS;

    public DefaultTransactionMonitoringBlockListener(ChainServicesContainer chainServicesContainer,
                                                     BlockchainEventBroadcaster broadcaster,
                                                     TransactionDetailsFactory transactionDetailsFactory,
                                                     BlockCache blockCache,
                                                     NodeSettings nodeSettings) {
        this.criteria = new ConcurrentHashMap<>();

        this.blockchainServices = new HashMap<>();

        chainServicesContainer
                .getNodeNames()
                .forEach(nodeName -> {
                    blockchainServices.put(nodeName,
                            chainServicesContainer.getNodeServices(nodeName).getBlockchainService());
                });

        this.broadcaster = broadcaster;
        this.transactionDetailsFactory = transactionDetailsFactory;
        this.blockCache = blockCache;
        this.nodeSettings = nodeSettings;
    }

    @Override
    public void onBlock(Block block) {
        lock.lock();

        try {
            if (ALL_TRANSACTIONS)
                processAllBlockTransactions(block);
            else
                processBlockMatchingTransactions(block);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void addMatchingCriteria(TransactionMatchingCriteria matchingCriteria) {

        lock.lock();

        try {
            final String nodeName = matchingCriteria.getNodeName();

            if (!criteria.containsKey(nodeName)) {
                criteria.put(nodeName, new CopyOnWriteArrayList<>());
            }

            criteria.get(nodeName).add(matchingCriteria);

            //Check if any cached blocks match
            //Note, this makes sense for tx hash but maybe doesn't for some other matchers?
            blockCache
                    .getCachedBlocks()
                    .forEach(block -> {
                        block.getTransactions().forEach(tx ->
                                broadcastIfMatched(tx, nodeName, Collections.singletonList(matchingCriteria)));
                    });
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeMatchingCriteria(TransactionMatchingCriteria matchingCriteria) {
        criteria.get(matchingCriteria.getNodeName()).remove(matchingCriteria);
    }

    private void processAllBlockTransactions(Block block) {
        block.getTransactions()
                .forEach(tx -> {
                    final TransactionDetails txDetails = transactionDetailsFactory.createTransactionDetails(
                            tx, TransactionStatus.CONFIRMED, block.getNodeName());
                    if (txDetails.getStatus().equals(TransactionStatus.CONFIRMED)) {
                        broadcaster.broadcastTransaction(txDetails);
                        getBlockchainService(block.getNodeName()).getTransactionReceipt(tx.getHash()).getLogs().forEach(log -> {
                            final LogDetails logDetails = transactionDetailsFactory.createLogDetails(log, block.getNodeName());
                            broadcaster.broadcastLog(logDetails);
                        });
                    }
                });
    }

    private void processBlockMatchingTransactions(Block block) {
        block.getTransactions()
                .forEach(tx -> broadcastIfMatched(tx, block.getNodeName()));
    }

    private void broadcastIfMatched(Transaction tx, String nodeName, List<TransactionMatchingCriteria> criteriaToCheck) {

        final TransactionDetails txDetails = transactionDetailsFactory.createTransactionDetails(
                tx, TransactionStatus.CONFIRMED, nodeName);

        //Only broadcast once, even if multiple matching criteria apply
        criteriaToCheck
                .stream()
                .filter(matcher -> matcher.isAMatch(txDetails))
                .findFirst()
                .ifPresent(matcher -> onTransactionMatched(txDetails, matcher));
    }

    private void broadcastIfMatched(Transaction tx, String nodeName) {
        if (criteria.containsKey(nodeName)) {
            broadcastIfMatched(tx, nodeName, criteria.get(nodeName));
        }
    }

    private void onTransactionMatched(TransactionDetails txDetails, TransactionMatchingCriteria matchingCriteria) {

        final Node node = nodeSettings.getNode(txDetails.getNodeName());
        final BlockchainService blockchainService = getBlockchainService(txDetails.getNodeName());

        final boolean isSuccess = isSuccessTransaction(txDetails);

        if (isSuccess && shouldWaitBeforeConfirmation(node)) {
            txDetails.setStatus(TransactionStatus.UNCONFIRMED);

            blockchainService.addBlockListener(new TransactionConfirmationBlockListener(txDetails,
                    blockchainService, broadcaster, node,
                    matchingCriteria.getStatuses(),
                    () -> onConfirmed(txDetails, matchingCriteria)));

            broadcastTransaction(txDetails, matchingCriteria);

            //Don't remove criteria if we're waiting for x blocks, as if there is a fork
            //we need to rebroadcast the unconfirmed tx in new block
        } else {
            if (!isSuccess) {
                txDetails.setStatus(TransactionStatus.FAILED);

                String reason = getRevertReason(txDetails);

                if (reason != null) {
                    txDetails.setRevertReason(reason);
                }
            }

            broadcastTransaction(txDetails, matchingCriteria);

            if (matchingCriteria.isOneTimeMatch()) {
                removeMatchingCriteria(matchingCriteria);
            }
        }
    }

    private void broadcastTransaction(TransactionDetails txDetails, TransactionMatchingCriteria matchingCriteria) {
        if (matchingCriteria.getStatuses().contains(txDetails.getStatus())) {
            broadcaster.broadcastTransaction(txDetails);
        }
    }

    private boolean isSuccessTransaction(TransactionDetails txDetails) {
        final TransactionReceipt receipt = getBlockchainService(txDetails.getNodeName())
                .getTransactionReceipt(txDetails.getHash());

        if (receipt.getStatus() == null) {
            // status is only present on Byzantium transactions onwards
            return true;
        }

        if (receipt.getStatus().equals("0x0")) {
            return false;
        }

        return true;
    }

    private boolean shouldWaitBeforeConfirmation(Node node) {
        return !node.getBlocksToWaitForConfirmation().equals(BigInteger.ZERO);
    }

    private BlockchainService getBlockchainService(String nodeName) {
        return blockchainServices.get(nodeName);
    }

    private void onConfirmed(TransactionDetails txDetails, TransactionMatchingCriteria matchingCriteria) {
        if (matchingCriteria.isOneTimeMatch()) {
            log.debug("Tx {} confirmed, removing matchingCriteria", txDetails.getHash());

            removeMatchingCriteria(matchingCriteria);
        }
    }


    private String getRevertReason(TransactionDetails txDetails) {
        Node node = nodeSettings.getNode(txDetails.getNodeName());

        if (!node.getAddTransactionRevertReason()) {
            return null;
        }

        return getBlockchainService(txDetails.getNodeName()).getRevertReason(
                txDetails.getFrom(),
                txDetails.getTo(),
                txDetails.getBlockNumber(),
                txDetails.getInput()
        );
    }
}
