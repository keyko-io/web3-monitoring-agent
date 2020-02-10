package io.keyko.monitoring.agent.core.chain.block;

import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.chain.settings.NodeSettings;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

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

    public ViewBlockListener(BlockchainService blockchainService,
                             BlockchainEventBroadcaster eventBroadcaster,
                             NodeSettings nodeSettings) {

        this.blockchainService = blockchainService;
        this.eventBroadcaster = eventBroadcaster;
        this.nodeSettings = nodeSettings;
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
                processViewMessage(filter);
            }
        });
    }

    private Function composeFunction(ContractViewFilter filter) {
        return filter.getMethodSpecification().getWeb3Function();
    }

    public void addViewFilter(ContractViewFilter filter) {
        log.debug(String.format(
                "Adding new filter to ViewBlockListener: %s", filter.getId()));
        if (!viewFilters.containsKey(filter.getId()))
            viewFilters.put(filter.getId(), filter);
    }

    public void processViewMessage(ContractViewFilter filter)  {
        // Compose message
        Function _func= filter.getMethodSpecification().getWeb3Function();
        log.debug("Processing message for filter " + filter.getId()
                + " and function " + _func.getName());

        // 3. Call the contract
        List<Type> result = blockchainService.executeReadCall(
                nodeSettings.getNode(filter.getNode()).getClientAddress(),
                filter.getContractAddress(),
                _func);

        log.info("--> Result after calling remote contract " + result.size());

        // 4. Parse the result
        if (result.size()>0)    {
            // 5. Write
            log.info("Returned: ");
            result.forEach( k -> log.info(k.getValue().toString()));
        }
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
