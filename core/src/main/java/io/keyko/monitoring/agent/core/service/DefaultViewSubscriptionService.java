package io.keyko.monitoring.agent.core.service;

import io.keyko.monitoring.agent.core.chain.block.ViewBlockListener;
import io.keyko.monitoring.agent.core.chain.contract.ContractEventListener;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.container.ChainServicesContainer;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.integration.broadcast.internal.EventeumEventBroadcaster;
import io.keyko.monitoring.agent.core.model.ViewFilterSubscription;
import io.keyko.monitoring.agent.core.repository.ContractViewFilterRepository;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import io.keyko.monitoring.agent.core.utils.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@inheritDoc}
 *
 */
@Slf4j
@Component
public class DefaultViewSubscriptionService implements ViewSubscriptionService {

    private ChainServicesContainer chainServices;

    private ContractViewFilterRepository viewFilterRepository;

    private EventeumEventBroadcaster eventeumEventBroadcaster;

    private AsyncTaskService asyncTaskService;

    private ViewBlockListener viewBlockListener;

    private Map<String, ViewFilterSubscription> filterSubscriptions = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    private RetryTemplate retryTemplate;

    @Autowired
    public DefaultViewSubscriptionService(ChainServicesContainer chainServices,
                                          ContractViewFilterRepository viewFilterRepository,
                                          EventeumEventBroadcaster eventeumEventBroadcaster,
                                          AsyncTaskService asyncTaskService,
                                          ViewBlockListener viewBlockListener,
                                          @Qualifier("eternalRetryTemplate") RetryTemplate retryTemplate) {
        this.chainServices = chainServices;
        this.asyncTaskService = asyncTaskService;
        this.viewFilterRepository = viewFilterRepository;
        this.eventeumEventBroadcaster = eventeumEventBroadcaster;
        this.viewBlockListener = viewBlockListener;
        this.retryTemplate = retryTemplate;
    }


    public void init() {
        chainServices.getNodeNames().forEach(nodeName ->
                subscribeToNewBlocks(
                        chainServices.getNodeServices(nodeName).getBlockchainService(), viewBlockListener));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContractViewFilter registerContractViewFilter(ContractViewFilter filter, boolean broadcast) {
        try {
            return doRegister(filter, broadcast);
        } catch (NotFoundException e) {
            log.error("Unable to register contract view filter " + e.getMessage());
            return null;
        }
    }


    private ContractViewFilter doRegister(ContractViewFilter filter, boolean broadcast) throws NotFoundException {
        populateIdIfMissing(filter);

        // Checking if we need to rewind old contract views
        try {
            if (null != filter.getStartBlock() && filter.getStartBlock().compareTo(BigInteger.ZERO) > 0 )  {
                // We need to catch previous views
                EthBlockNumber blockNumber = chainServices.getNodeServices(filter.getNode()).getWeb3j().ethBlockNumber().send();
                if (filter.getStartBlock().compareTo(blockNumber.getBlockNumber()) < 0)   {
                    viewBlockListener.processPreviousBlockViews(filter, filter.getStartBlock(), blockNumber.getBlockNumber());
                }
            }
        } catch (IOException e) {
            log.error("Unable to replay views: " + e.getMessage());
        }

        saveContractViewFilter(filter);
        viewBlockListener.addViewFilter(filter);

        filterSubscriptions.put(filter.getId(), new ViewFilterSubscription(filter));

        if (broadcast) {
            broadcastContractEventFilterAdded(filter);
        }

        return filter;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public ContractViewFilter registerContractViewFilterWithRetries(ContractViewFilter filter, boolean broadcast) {
        try {
            return retryTemplate.execute((context) -> doRegister(filter, broadcast));
        } catch (NotFoundException e) {
            log.error("Unable to register contract view filter with retries " + e.getMessage());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ContractViewFilter> listContractViewFilters() {
        List<ContractViewFilter> target = new ArrayList<>();
        viewFilterRepository.findAll().forEach(target::add);
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContractViewFilter getContractViewFilter(String filterId) throws NotFoundException {
        Optional<ContractViewFilter> byId = viewFilterRepository.findById(filterId);
        if (!byId.isPresent() || null == byId.get()) {
            throw new NotFoundException(String.format("Filter with id %s, doesn't exist", filterId));
        }
        return byId.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContractViewFilter(String filterId) throws NotFoundException {
        unregisterContractViewFilter(filterId, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContractViewFilter(String filterId, boolean broadcast) throws NotFoundException {
//        final ViewFilterSubscription filterSubscription = getFilterSubscription(filterId);

        Optional<ContractViewFilter> byId = viewFilterRepository.findById(filterId);

        if (!byId.isPresent() || null == byId.get()) {
            throw new NotFoundException(String.format("Filter with id %s, doesn't exist", filterId));
        }

        deleteContractViewFilter(byId.get());
        viewBlockListener.removeViewFilter(byId.get());
        filterSubscriptions.remove(byId.get());

        if (broadcast) {
            broadcastContractViewFilterRemoved(byId.get());
        }
    }

    @Override
    public void resubscribeToAllSubscriptions() {
        final List<ContractViewFilter> dbFilters = listContractViewFilters();

        dbFilters.forEach( viewFilter -> {
            try {
                log.info("Re-Subscribing to View Filter: " + viewFilter.getId());
                doRegister(viewFilter, false);
            } catch (NotFoundException e) {
                log.error("Unable to re-subscribe to ViewFilter "+ viewFilter.getId());
            }
        });

        log.info("Resubscribed to view filters: {}", JSON.stringify(filterSubscriptions));

    }

    @Override
    public void unsubscribeToAllSubscriptions(String nodeName) {

    }

    private void subscribeToNewBlocks(
            BlockchainService blockchainService, ViewBlockListener viewBlockListener) {
        blockchainService.addBlockListener(viewBlockListener);
        blockchainService.connect();
    }

    private void triggerListener(ContractEventListener listener, ContractEventDetails contractEventDetails) {
        try {
//            listener.onEvent(contractEventDetails);
        } catch (Throwable t) {
            log.error(String.format(
                    "An error occurred when processing contractEvent with id %s", contractEventDetails.getId()), t);
        }
    }

    private ContractViewFilter saveContractViewFilter(ContractViewFilter filter) {
        return viewFilterRepository.save(filter);
    }

    private void deleteContractViewFilter(ContractViewFilter filter) {
        viewFilterRepository.deleteById(filter.getId());
    }

    private void broadcastContractEventFilterAdded(ContractViewFilter filter) {
        eventeumEventBroadcaster.broadcastViewFilterAdded(filter);
    }

    private void broadcastContractViewFilterRemoved(ContractViewFilter filter) {
        eventeumEventBroadcaster.broadcastViewFilterRemoved(filter);
    }

    private boolean isFilterRegistered(ContractViewFilter filter) {
        return viewBlockListener.isViewFilterRegistered(filter.getId());
//        return (getFilterSubscription(filter.getId()) != null);
    }

    //    private ViewFilterSubscription getFilterSubscription(String filterId) {
//        return filterSubscriptions.get(filterId);
//    }
//
    @Override
    public List<ViewFilterSubscription> getFilterSubscriptions() {
        return new ArrayList(filterSubscriptions.values());
    }
//
//    private void removeFilterSubscription(String filterId) {
//        filterSubscriptions.remove(filterId);
//    }

    private void populateIdIfMissing(ContractViewFilter filter) {
        if (filter.getId() == null) {
            filter.setId(UUID.randomUUID().toString());
        }
    }
}
