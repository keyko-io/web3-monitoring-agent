package io.keyko.monitoring.agent.core.endpoint;

import io.keyko.common.helpers.AbiParser;
import io.keyko.common.models.EthereumAbi;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.endpoint.response.AbiImportResponse;
import io.keyko.monitoring.agent.core.endpoint.response.AddEventFilterResponse;
import io.keyko.monitoring.agent.core.endpoint.response.FilterEndpointException;
import io.keyko.monitoring.agent.core.service.AbiImportingService;
import io.keyko.monitoring.agent.core.service.EventSubscriptionService;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A REST endpoint for importing agent filters given a contract ABI
 *
 */
@RestController
@RequestMapping(value = "/api/rest/v1/abi-importer")
@AllArgsConstructor
public class AbiImporterEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(AbiImporterEndpoint.class);
    private AbiImportingService abiImportingService;

    /**
     * Adds an event filter with the specification described in the ContractEventFilter.
     *
     * @param abi the abi to import
     * @param filterType Type of filter to register (event, view or all)
     * @param blockInterval If given the block interval to ask for a view
     * @param startBlock If given the block since we fetch the events or view data
     * @return AbiImportResponse
     */
    @RequestMapping(method = RequestMethod.POST)
    public AbiImportResponse importAbiFilters(@RequestBody String abi,
                                            @RequestParam(required = false, defaultValue = "events") String filterType,
                                            @RequestParam(required = false, defaultValue = "100") Integer blockInterval,
                                            @RequestParam(required = false, defaultValue = "-1") Integer startBlock) {

        AbiImportResponse importResponse;
        try {
            final AbiParser abiParser = AbiParser.load(abi);
            importResponse = abiImportingService.importAbi(abiParser, filterType, blockInterval, startBlock);
        } catch (IOException e) {
            throw new FilterEndpointException(e.getMessage());
        }

        return importResponse;
    }


}
