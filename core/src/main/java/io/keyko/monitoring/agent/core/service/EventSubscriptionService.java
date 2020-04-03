package io.keyko.monitoring.agent.core.service;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.model.EventFilterSubscription;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;

import java.util.List;

/**
 * A service for manageing contract event subscriptions within the Eventeum instance.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
public interface EventSubscriptionService {

    /**
     * Initialise the subscription service
     */
    void init();

    /**
     * Registers a new contract event filter.
     * <p>
     * If the id is null, then one is assigned.
     *
     * @param filter    The filter to add.
     * @param broadcast Specifies if the added filter event should be broadcast to other Eventeum instances.
     * @return The registered contract event filter
     */
    ContractEventFilter registerContractEventFilter(ContractEventFilter filter, boolean broadcast);

    /**
     * Registers a new contract event filter.
     * <p>
     * If the id is null, then one is assigned.
     * <p>
     * Will retry indefinitely until successful
     *
     * @param filter    The filter to add.
     * @param broadcast Specifies if the added filter event should be broadcast to other Eventeum instances.
     * @return The registered contract event filter
     */
    ContractEventFilter registerContractEventFilterWithRetries(ContractEventFilter filter, boolean broadcast);

    /**
     * List all registered contract event filters.
     *
     * @return The list of registered contract event filters
     */
    List<ContractEventFilter> listContractEventFilters();


    /**
     * Get a previously added contract event filter.
     *
     * @param filterId  The filter id of the event to return.
     * @return ContractEventFilter
     * @throws NotFoundException object not found
     */
    ContractEventFilter getContractEventFilter(String filterId) throws NotFoundException;

    /**
     * Unregisters a previously added contract event filter.
     * <p>
     * Broadcasts the removed filter event to any other Eventeum instances.
     *
     * @param filterId The filter id of the event to remove.
     * @throws NotFoundException object not found
     */
    void unregisterContractEventFilter(String filterId) throws NotFoundException;

    /**
     * Unregisters a previously added contract event filter.
     *
     * @param filterId  The filter id of the event to remove.
     * @param broadcast Specifies if the removed filter event should be broadcast to other Eventeum instances.
     * @throws NotFoundException object not found
     */
    void unregisterContractEventFilter(String filterId, boolean broadcast) throws NotFoundException;

    /**
     * Resubscribe to all currently active event filters.
     */
    void resubscribeToAllSubscriptions();

    /**
     * Unsubscribe all active listeners
     * @param nodeName name of the node
     */
    void unsubscribeToAllSubscriptions(String nodeName);

    /**
     * Returns true if all subscriptions for node are active (not disposed)
     *
     * @param nodeName The node name
     * @return true if all subscriptions for node are active (not disposed)
     */
    boolean isFullySubscribed(String nodeName);

    /**
     * Get the list of all the subscription filters from memory
     * @return EventFilterSubscription
     */
    List<EventFilterSubscription> getFilterSubscriptions();
}
