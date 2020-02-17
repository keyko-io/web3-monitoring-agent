package io.keyko.monitoring.agent.core.endpoint;

import io.keyko.monitoring.agent.core.endpoint.response.MonitorTransactionsResponse;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import io.keyko.monitoring.agent.core.service.TransactionMonitoringService;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * A REST endpoint for adding a removing event filters.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@RestController
@RequestMapping(value = "/api/rest/v1/transaction")
@AllArgsConstructor
public class TransactionMonitoringEndpoint {

    private TransactionMonitoringService monitoringService;

    /**
     * Monitors a transaction with the specified hash, on a specific node
     *
     * @param spec the transaction spec to add
     * @param response the http response
     * @return MonitorTransactionsResponse
     */
    @RequestMapping(method = RequestMethod.POST)
    public MonitorTransactionsResponse monitorTransactions(@RequestBody TransactionMonitoringSpec spec,
                                                           HttpServletResponse response) {
        spec.generateId();
        spec.convertToCheckSum();
        monitoringService.registerTransactionsToMonitor(spec);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);

        return new MonitorTransactionsResponse(spec.getId());
    }

    /**
     * Stops monitoring a transaction with the specfied hash
     *
     * @param id   specId the id of the transaction monitor to remove
     * @param nodeName the name of the node where the transaction is being monitored
     * @param response the http response
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void stopMonitoringTransaction(@PathVariable String id,
                                          @RequestParam(required = false) String nodeName,
                                          HttpServletResponse response) {

        try {
            monitoringService.stopMonitoringTransactions(id);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NotFoundException e) {
            //Rethrow endpoint exception with response information
            throw new TransactionNotFoundEndpointException();
        }
    }
}