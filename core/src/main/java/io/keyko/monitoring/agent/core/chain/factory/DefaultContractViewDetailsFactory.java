package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.chain.converter.EventParameterConverter;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.chain.settings.Node;
import io.keyko.monitoring.agent.core.chain.util.Web3jUtil;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventStatus;
import io.keyko.monitoring.agent.core.dto.event.filter.*;
import io.keyko.monitoring.agent.core.dto.event.parameter.EventParameter;
import io.keyko.monitoring.agent.core.dto.view.ContractViewDetails;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class DefaultContractViewDetailsFactory implements ContractViewDetailsFactory {

    private EventParameterConverter<Type> parameterConverter;
    private Node node;
    private String networkName;

    public DefaultContractViewDetailsFactory(EventParameterConverter<Type> parameterConverter,
                                             Node node,
                                             String networkName) {
        this.parameterConverter = parameterConverter;
        this.node = node;
        this.networkName = networkName;
    }

    @Override
    public ContractViewDetails createViewDetails(ContractViewFilter filter, List<Type> returnedFromCall, Block block) {

        List<EventParameter> functionOutput= new ArrayList<>();
        int position= 0;
        returnedFromCall.forEach( _type -> {
            log.trace("Converting Result to EventParameter: " + _type.getValue().toString());
            String _name= filter.getMethodSpecification().getOutputParameterDefinitions().get(position).getName();
            EventParameter eventParameter = parameterConverter.convert(_type);
            eventParameter.setName(_name);
            functionOutput.add(eventParameter);
        });

        ContractViewDetails details= new ContractViewDetails();
        details.setFilterId(filter.getId());
        details.setName(filter.getMethodSpecification().getMethodName());
        details.setContractName(filter.getMethodSpecification().getContractName());
        details.setNodeName(filter.getNode());
        details.setOutput(functionOutput);
        details.setAddress(filter.getContractAddress());

        details.setBlockNumber(block.getNumber());
        details.setBlockHash(block.getHash());
        details.setNetworkName(networkName);
        return details;
    }

}