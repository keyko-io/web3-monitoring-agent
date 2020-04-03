package io.keyko.monitoring.agent.core.endpoint;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.endpoint.response.AddEventFilterResponse;
import io.keyko.monitoring.agent.core.service.EventSubscriptionService;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * A REST endpoint for adding a removing event filters.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@RestController
@RequestMapping(value = "/api/rest/v1/event-filter")
@AllArgsConstructor
public class ContractEventFilterEndpoint {

    private EventSubscriptionService filterService;

    /**
     * Adds an event filter with the specification described in the ContractEventFilter.
     *
     * @param eventFilter the event filter to add
     * @param response    the http response
     * @return AddEventFilterResponse
     */
    @RequestMapping(method = RequestMethod.POST)
    public AddEventFilterResponse addEventFilter(@RequestBody ContractEventFilter eventFilter,
                                                 HttpServletResponse response) {

        final ContractEventFilter registeredFilter = filterService.registerContractEventFilter(eventFilter, true);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);

        return new AddEventFilterResponse(registeredFilter.getId());
    }

    /**
     * Returns the list of registered {@link ContractEventFilter}
     *
     * @param response the http response
     * @return list of ContractEventFilter
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<ContractEventFilter> listEventFilters(HttpServletResponse response) {
        List<ContractEventFilter> registeredFilters = filterService.listContractEventFilters();
        response.setStatus(HttpServletResponse.SC_OK);

        return registeredFilters;
    }

    /**
     * Get a event filter with the corresponding filter id.
     *
     * @param filterId the filterId to get
     * @param response the http response
     * @return ContractViewFilter
     */
    @RequestMapping(value = "/{filterId}", method = RequestMethod.GET)
    public ContractEventFilter getViewFilter(@PathVariable String filterId,
                                             HttpServletResponse response) {

        try {
            ContractEventFilter filter = filterService.getContractEventFilter(filterId);
            response.setStatus(HttpServletResponse.SC_OK);
            return filter;
        } catch (NotFoundException e) {
            //Rethrow endpoint exception with response information
            throw new FilterNotFoundEndpointException();
        }
    }

    /**
     * Deletes an event filter with the corresponding filter id.
     *
     * @param filterId the filterId to delete
     * @param response the http response
     */
    @RequestMapping(value = "/{filterId}", method = RequestMethod.DELETE)
    public void removeEventFilter(@PathVariable String filterId,
                                  HttpServletResponse response) {

        try {
            filterService.unregisterContractEventFilter(filterId, true);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NotFoundException e) {
            //Rethrow endpoint exception with response information
            throw new FilterNotFoundEndpointException();
        }
    }
}
