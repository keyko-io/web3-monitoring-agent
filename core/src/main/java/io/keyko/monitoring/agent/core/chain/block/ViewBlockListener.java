package io.keyko.monitoring.agent.core.chain.block;

import io.keyko.monitoring.agent.core.chain.converter.Web3jEventParameterConverter;
import io.keyko.monitoring.agent.core.chain.factory.ContractViewDetailsFactory;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.chain.settings.NodeSettings;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.dto.view.ContractViewDetails;
import io.keyko.monitoring.agent.core.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A block listener that manage the view requests after blocks are mined
 *
 */
@Component
public class ViewBlockListener implements BlockListener {

    private static final Logger log = LoggerFactory.getLogger(ViewBlockListener.class);

    private BlockchainService blockchainService;
    private BlockchainEventBroadcaster eventBroadcaster;
    private Map<String, ContractViewFilter> viewFilters= new HashMap<>();
    private NodeSettings nodeSettings;
    private Web3jEventParameterConverter eventParameterConverter;
    private ContractViewDetailsFactory contractViewDetailsFactory;

    public ViewBlockListener(BlockchainService blockchainService,
                             BlockchainEventBroadcaster eventBroadcaster,
                             NodeSettings nodeSettings,
                             Web3jEventParameterConverter eventParameterConverter,
                             ContractViewDetailsFactory contractViewDetailsFactory) {

        this.blockchainService = blockchainService;
        this.eventBroadcaster = eventBroadcaster;
        this.nodeSettings = nodeSettings;
        this.eventParameterConverter= eventParameterConverter;
        this.contractViewDetailsFactory = contractViewDetailsFactory;
    }

    @Override
    public void onBlock(Block block) {
        log.info(String.format("New block mined. Hash: %s, Number: %s, Filters: %s",
                block.getHash(), block.getNumber(), viewFilters.size()));

        // Foreach registered filter
        viewFilters.forEach((id, filter) -> {
            BigInteger interval= BigInteger.valueOf(
                    filter.getPollingStrategy().getBlockInterval().intValue());

            // Check if blockInterval requires req
            BigInteger mod= block.getNumber().mod(interval);

            if (mod.equals(BigInteger.ZERO))    {
                processViewMessage(filter, block);
            }
        });
    }

    private Function composeFunction(ContractViewFilter filter) throws UnsupportedEncodingException {
        return filter.getMethodSpecification().getWeb3Function();
    }

    public void addViewFilter(ContractViewFilter filter) {
        log.debug(String.format(
                "Adding new filter to ViewBlockListener: %s", filter.getId()));
        if (!viewFilters.containsKey(filter.getId()))
            viewFilters.put(filter.getId(), filter);
    }

    public boolean processViewMessage(ContractViewFilter filter, Block block)  {
        try {
            // Compose message
            Function _func= composeFunction(filter);
            log.debug("Processing message for filter " + filter.getId()
                    + " and function " + _func.getName());

            // 3. Call the contract
            List<Type> result = blockchainService.executeReadCall(
                    filter.getContractAddress(),
                    _func);


            log.debug("Number of items returned after calling remote contract: " + result.size());

            int expectedNumberResults= filter.getMethodSpecification().getOutputParameterDefinitions().size();
            if (result.size() != expectedNumberResults)    {
                log.error(String.format("Un-expected number of results. Expected %s return values but found %s.",
                        expectedNumberResults, result.size()));
                return false;
            }
            if (result.size()>0)    {
                // 4. Parse the result
                ContractViewDetails viewDetails = contractViewDetailsFactory.createViewDetails(filter, result, block);
                // Broadcasting
                eventBroadcaster.broadcastContractView(viewDetails);
                return true;
            }
        } catch (Exception e)   {
            log.error("Unable to process view message: " + e.getMessage());
        }
        return false;
    }

    public void removeViewFilter(ContractViewFilter filter) {
        log.debug(String.format(
                "Removing filter from ViewBlockListener: %s", filter.getId()));
        if (viewFilters.containsKey(filter.getId()))
            viewFilters.remove(filter.getId());
    }

    public ContractViewFilter getViewFilter(String id) throws NotFoundException {
        if (viewFilters.containsKey(id))
            return viewFilters.get(id);
        throw new NotFoundException("NotFound View Filter with id " + id);
    }

    public boolean isViewFilterRegistered(String id)    {
        return viewFilters.containsKey(id);
    }

}
