package io.keyko.monitoring.agent.core.service;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.model.ViewFilterSubscription;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;

import java.util.List;

/**
 * A service for managing contract public method subscriptions within the Eventeum instance.
 *
 */
public interface ViewSubscriptionService {

    /**
     * Initialise the subscription service
     */
    void init();

    /**
     * Registers a new contract view filter.
     * <p>
     * If the id is null, then one is assigned.
     *
     * @param filter    The filter to add.
     * @param broadcast Specifies if the added filter view should be broadcast to other Eventeum instances.
     * @return The registered contract view filter
     */
    ContractViewFilter registerContractViewFilter(ContractViewFilter filter, boolean broadcast);

    /**
     * Registers a new contract view filter.
     * <p>
     * If the id is null, then one is assigned.
     * <p>
     * Will retry indefinitely until successful
     *
     * @param filter    The filter to add.
     * @param broadcast Specifies if the added filter view should be broadcast to other Eventeum instances.
     * @return The registered contract view filter
     */
    ContractViewFilter registerContractViewFilterWithRetries(ContractViewFilter filter, boolean broadcast);

    /**
     * List all registered contract view filters.
     *
     * @return The list of registered contract view filters
     */
    List<ContractViewFilter> listContractViewFilters();

    /**
     * Get a previously added contract view filter.
     *
     * @param filterId  The filter id of the view to return.
     * @return ContractViewFilter
     * @throws NotFoundException object not found
     */
    ContractViewFilter getContractViewFilter(String filterId) throws NotFoundException;

    /**
     * Unregisters a previously added contract view filter.
     * <p>
     * Broadcasts the removed filter view to any other Eventeum instances.
     *
     * @param filterId The filter id of the view to remove.
     * @throws NotFoundException object not found
     */
    void unregisterContractViewFilter(String filterId) throws NotFoundException;

    /**
     * Unregisters a previously added contract view filter.
     *
     * @param filterId  The filter id of the view to remove.
     * @param broadcast Specifies if the removed filter view should be broadcast to other Eventeum instances.
     * @throws NotFoundException object not found
     */
    void unregisterContractViewFilter(String filterId, boolean broadcast) throws NotFoundException;

    /**
     * Resubscribe to all currently active event filters.
     */
    void resubscribeToAllSubscriptions();

    /**
     * Unsubscribe all active listeners
     * @param nodeName the name of the node
     */
    void unsubscribeToAllSubscriptions(String nodeName);

    /**
     * Get the list of all the subscription filters from memory
     * @return ViewFilterSubscription
     */
    List<ViewFilterSubscription> getFilterSubscriptions();
}
