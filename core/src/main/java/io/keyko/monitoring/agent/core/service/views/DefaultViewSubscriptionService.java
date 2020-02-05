package io.keyko.monitoring.agent.core.service.views;

import io.keyko.monitoring.agent.core.chain.block.BlockListener;
import io.keyko.monitoring.agent.core.chain.block.LoggingBlockListener;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.container.ChainServicesContainer;
import io.keyko.monitoring.agent.core.chain.service.container.NodeServices;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.endpoint.ContractViewFilterRepository;
import io.keyko.monitoring.agent.core.integration.broadcast.internal.EventeumMessageBroadcaster;
import io.keyko.monitoring.agent.core.model.FilterSubscription;
import io.keyko.monitoring.agent.core.service.AbstractSubscriptionService;
import io.keyko.monitoring.agent.core.service.AsyncTaskService;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import io.keyko.monitoring.agent.core.utils.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@inheritDoc}
 *
 */
@Slf4j
@Component
public class DefaultViewSubscriptionService extends AbstractSubscriptionService implements ViewSubscriptionService {

    private ChainServicesContainer chainServices;

    private ContractViewFilterRepository viewFilterRepository;

    private EventeumMessageBroadcaster messageBroadcaster;

    private AsyncTaskService asyncTaskService;

    private List<BlockListener> blockListeners;

    private Map<String, FilterSubscription> filterSubscriptions = new ConcurrentHashMap<>();

    private RetryTemplate retryTemplate;

    @Autowired
    public DefaultViewSubscriptionService(ChainServicesContainer chainServices,
                                          ContractViewFilterRepository viewFilterRepository,
                                          EventeumMessageBroadcaster messageBroadcaster,
                                          AsyncTaskService asyncTaskService,
                                          List<BlockListener> blockListeners,
                                          @Qualifier("eternalRetryTemplate") RetryTemplate retryTemplate) {
        this.chainServices = chainServices;
        this.asyncTaskService = asyncTaskService;
        this.viewFilterRepository = viewFilterRepository;
        this.messageBroadcaster = messageBroadcaster;
        this.blockListeners = blockListeners;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public void init() {
        chainServices.getNodeNames().forEach(nodeName -> subscribeToNewBlockEvents(
                chainServices.getNodeServices(nodeName).getBlockchainService(), blockListeners));
    }

    @Override
    public ContractViewFilter registerContractViewFilter(ContractViewFilter filter, boolean broadcast) {
        return doRegisterContractViewFilter(filter, broadcast);
    }

    @Override
    public ContractViewFilter registerContractViewFilterWithRetries(ContractViewFilter filter, boolean broadcast) {
        return null;
    }

    @Override
    public List<ContractViewFilter> listContractViewFilters() {
        return null;
    }

    @Override
    public void unregisterContractViewFilter(String filterId) throws NotFoundException {

    }

    @Override
    public void unregisterContractViewFilter(String filterId, boolean broadcast) throws NotFoundException {

    }

    @Override
    public void resubscribeToAllSubscriptions() {

    }

    @Override
    public void unsubscribeToAllSubscriptions(String nodeName) {

    }

    @Override
    public boolean isFullySubscribed(String nodeName) {
        return false;
    }

    private ContractViewFilter doRegisterContractViewFilter(ContractViewFilter filter, boolean broadcast) {
        populateIdIfMissing(filter);

        if (!isFilterRegistered(filter)) {
            final FilterSubscription sub = registerContractViewFilter(filter, filterSubscriptions);

            if (filter.getStartBlock() == null && sub != null) {
                filter.setStartBlock(sub.getStartBlock());
            }

            saveContractViewFilter(filter);

            if (broadcast) {
                broadcastContractViewFilterAdded(filter);
            }

            return filter;
        } else {
            log.info("Already registered contract event filter with id: " + filter.getId());
            return getFilterSubscription(filter.getId()).getFilter();
        }
    }

    private void registerContractViewFilter(ContractViewFilter filter, Map<String, FilterSubscription> allFilterSubscriptions) {
        log.info("Registering filter: " + JSON.stringify(filter));

        final NodeServices nodeServices = chainServices.getNodeServices(filter.getNode());

        if (nodeServices == null) {
            log.warn("No node configure" +
                    "d with name {}, not registering filter", filter.getNode());
            return null;
        }

        final BlockchainService blockchainService = nodeServices.getBlockchainService();

        blockchainService.addBlockListener(new LoggingBlockListener());

        log.debug("Registered filters: {}", JSON.stringify(allFilterSubscriptions));

        return sub;
    }

    private ContractViewFilter saveContractViewFilter(ContractViewFilter filter) {
        return viewFilterRepository.save(filter);
    }

    private void broadcastContractViewFilterAdded(ContractViewFilter filter) {
        messageBroadcaster.broadcastViewFilterAdded(filter);
    }

    private void populateIdIfMissing(ContractViewFilter filter) {
        if (filter.getId() == null) {
            filter.setId(UUID.randomUUID().toString());
        }
    }

    private boolean isFilterRegistered(ContractViewFilter filter) {
        return (getFilterSubscription(filter.getId()) != null);
    }
}
