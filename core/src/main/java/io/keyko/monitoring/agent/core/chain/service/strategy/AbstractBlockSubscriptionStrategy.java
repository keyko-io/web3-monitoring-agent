package io.keyko.monitoring.agent.core.chain.service.strategy;

import io.keyko.monitoring.agent.core.chain.ChainBootstrapper;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import io.keyko.monitoring.agent.core.utils.ExecutorNameFactory;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import lombok.extern.slf4j.Slf4j;
import io.keyko.monitoring.agent.core.chain.block.BlockListener;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.service.AsyncTaskService;
import io.keyko.monitoring.agent.core.service.EventStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.net.SocketException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public abstract class AbstractBlockSubscriptionStrategy<T> implements BlockSubscriptionStrategy {

    private final Logger LOG = LoggerFactory.getLogger(AbstractBlockSubscriptionStrategy.class);
    protected static final String BLOCK_EXECUTOR_NAME = "BLOCK";

    protected Collection<BlockListener> blockListeners = new ConcurrentLinkedQueue<>();
    protected Disposable blockSubscription;
    protected Web3j web3j;
    protected EventStoreService eventStoreService;
    protected String nodeName;
    protected AsyncTaskService asyncService;

    public AbstractBlockSubscriptionStrategy(Web3j web3j,
                                             String nodeName,
                                             EventStoreService eventStoreService,
                                             AsyncTaskService asyncService) {
        this.web3j = web3j;
        this.nodeName = nodeName;
        this.eventStoreService = eventStoreService;
        this.asyncService = asyncService;

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if ((e instanceof IOException) || (e instanceof SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                LOG.error("Error application");
                return;
            }
            if (e instanceof IllegalStateException) {
                LOG.error("Error in RxJava or in a custom operator");
                return;
            }
            LOG.warn("Undeliverable exception received, not sure what to do", e);
        });
    }

    @Override
    public void unsubscribe() {
        try {
            if (blockSubscription != null) {
                blockSubscription.dispose();
            }
        } catch (UndeliverableException ex) {
        } finally {
            blockSubscription = null;
        }
    }

    @Override
    public void addBlockListener(BlockListener blockListener) {
        blockListeners.add(blockListener);
    }

    @Override
    public void removeBlockListener(BlockListener blockListener) {
        blockListeners.remove(blockListener);
    }

    public boolean isSubscribed() {
        return blockSubscription != null && !blockSubscription.isDisposed();
    }

    protected void triggerListeners(T blockObject) {
        final Block eventeumBlock = convertToEventeumBlock(blockObject);
        triggerListeners(eventeumBlock);
    }

    protected void triggerListeners(Block eventeumBlock) {
        asyncService.execute(ExecutorNameFactory.build(BLOCK_EXECUTOR_NAME, eventeumBlock.getNodeName()), () -> {
            blockListeners.forEach(listener -> triggerListener(listener, eventeumBlock));
        });
    }

    protected void triggerListener(BlockListener listener, Block block) {
        try {
            listener.onBlock(block);
        } catch(Throwable t) {
            log.error(String.format("An error occured when processing block with hash %s", block.getHash()), t);
        }
    }

    protected Optional<LatestBlock> getLatestBlock() {
        return eventStoreService.getLatestBlock(nodeName);
    }

    abstract Block convertToEventeumBlock(T blockObject);

}
