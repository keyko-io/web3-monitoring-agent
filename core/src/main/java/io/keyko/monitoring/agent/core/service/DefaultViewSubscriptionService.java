package io.keyko.monitoring.agent.core.service;

import io.keyko.monitoring.agent.core.chain.block.BlockListener;
import io.keyko.monitoring.agent.core.chain.contract.ContractEventListener;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.container.ChainServicesContainer;
import io.keyko.monitoring.agent.core.chain.service.container.NodeServices;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.integration.broadcast.internal.EventeumEventBroadcaster;
import io.keyko.monitoring.agent.core.model.EventFilterSubscription;
import io.keyko.monitoring.agent.core.model.ViewFilterSubscription;
import io.keyko.monitoring.agent.core.repository.ContractViewFilterRepository;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import io.keyko.monitoring.agent.core.utils.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    private List<BlockListener> blockListeners;

    private Map<String, ViewFilterSubscription> filterSubscriptions = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    private RetryTemplate retryTemplate;

    @Autowired
    public DefaultViewSubscriptionService(ChainServicesContainer chainServices,
                                          ContractViewFilterRepository viewFilterRepository,
                                          EventeumEventBroadcaster eventeumEventBroadcaster,
                                          AsyncTaskService asyncTaskService,
                                          List<BlockListener> blockListeners,
                                          @Qualifier("eternalRetryTemplate") RetryTemplate retryTemplate) {
        this.chainServices = chainServices;
        this.asyncTaskService = asyncTaskService;
        this.viewFilterRepository = viewFilterRepository;
        this.eventeumEventBroadcaster = eventeumEventBroadcaster;
        this.blockListeners = blockListeners;
        this.retryTemplate = retryTemplate;
    }


    public void init() {
        chainServices.getNodeNames().forEach(nodeName ->
                subscribeToNewBlockEvents(
                    chainServices.getNodeServices(nodeName).getBlockchainService(), blockListeners));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContractViewFilter registerContractViewFilter(ContractViewFilter filter, boolean broadcast) {
        return doRegister(filter, broadcast);
    }


    private ContractViewFilter doRegister(ContractViewFilter filter, boolean broadcast) {
        populateIdIfMissing(filter);

        if (!isFilterRegistered(filter)) {

            saveContractViewFilter(filter);
//            filterSubscriptions.put(filter.getId(), filter);

            if (broadcast) {
                broadcastContractEventFilterAdded(filter);
            }

            return filter;
        } else {
            log.info("Already registered contract event filter with id: " + filter.getId());
            return getFilterSubscription(filter.getId()).getFilter();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public ContractViewFilter registerContractViewFilterWithRetries(ContractViewFilter filter, boolean broadcast) {
        return retryTemplate.execute((context) -> doRegister(filter, broadcast));
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

        if (broadcast) {
            broadcastContractViewFilterRemoved(byId.get());
        }
    }

    private void subscribeToNewBlockEvents(
            BlockchainService blockchainService, List<BlockListener> blockListeners) {
        blockListeners.forEach(listener -> blockchainService.addBlockListener(listener));

        blockchainService.connect();
    }

    private void triggerListener(ContractEventListener listener, ContractEventDetails contractEventDetails) {
        try {
            listener.onEvent(contractEventDetails);
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
        return (getFilterSubscription(filter.getId()) != null);
    }

    private ViewFilterSubscription getFilterSubscription(String filterId) {
        return filterSubscriptions.get(filterId);
    }

    private List<ViewFilterSubscription> getFilterSubscriptions() {
        return new ArrayList(filterSubscriptions.values());
    }

    private void removeFilterSubscription(String filterId) {
        filterSubscriptions.remove(filterId);
    }

    private void populateIdIfMissing(ContractViewFilter filter) {
        if (filter.getId() == null) {
            filter.setId(UUID.randomUUID().toString());
        }
    }
}
