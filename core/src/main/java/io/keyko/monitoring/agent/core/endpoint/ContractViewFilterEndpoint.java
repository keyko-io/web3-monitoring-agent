package io.keyko.monitoring.agent.core.endpoint;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.endpoint.response.AddViewFilterResponse;
import io.keyko.monitoring.agent.core.endpoint.response.FilterNotFoundEndpointException;
import io.keyko.monitoring.agent.core.service.ViewSubscriptionService;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * A REST endpoint for adding a removing view filters.
 *
 */
@RestController
@RequestMapping(value = "/api/rest/v1/view-filter")
@AllArgsConstructor
public class ContractViewFilterEndpoint {

    private ViewSubscriptionService filterService;

    /**
     * Adds a view filter with the specification described in the ContractViewFilter.
     *
     * @param viewFilter the view filter to add
     * @param response    the http response
     * @return AddViewFilterResponse
     */
    @RequestMapping(method = RequestMethod.POST)
    public AddViewFilterResponse addViewFilter(@RequestBody ContractViewFilter viewFilter,
                                               HttpServletResponse response) {

        final ContractViewFilter registeredFilter = filterService.registerContractViewFilter(viewFilter, true);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);

        return new AddViewFilterResponse(registeredFilter.getId());
    }

    /**
     * Returns the list of registered {@link ContractViewFilter}
     *
     * @param response the http response
     * @return list of ContractViewFilter
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<ContractViewFilter> listViewFilters(HttpServletResponse response) {
        List<ContractViewFilter> registeredFilters = filterService.listContractViewFilters();
        response.setStatus(HttpServletResponse.SC_OK);

        return registeredFilters;
    }

    /**
     * Get a view filter with the corresponding filter id.
     *
     * @param filterId the filterId to delete
     * @param response the http response
     * @return ContractViewFilter
     */
    @RequestMapping(value = "/{filterId}", method = RequestMethod.GET)
    public ContractViewFilter getViewFilter(@PathVariable String filterId,
                                 HttpServletResponse response) {

        try {
            ContractViewFilter filter= filterService.getContractViewFilter(filterId);
            response.setStatus(HttpServletResponse.SC_OK);
            return filter;
        } catch (NotFoundException e) {
            //Rethrow endpoint exception with response information
            throw new FilterNotFoundEndpointException();
        }
    }

    /**
     * Deletes a view filter with the corresponding filter id.
     *
     * @param filterId the filterId to delete
     * @param response the http response
     */
    @RequestMapping(value = "/{filterId}", method = RequestMethod.DELETE)
    public void removeViewFilter(@PathVariable String filterId,
                                 HttpServletResponse response) {

        try {
            filterService.unregisterContractViewFilter(filterId, true);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NotFoundException e) {
            //Rethrow endpoint exception with response information
            throw new FilterNotFoundEndpointException();
        }
    }
}
