package io.keyko.monitoring.agent.core.service;

import io.keyko.common.helpers.AbiParser;
import io.keyko.common.models.EthereumAbi;
import io.keyko.monitoring.agent.core.chain.block.ViewBlockListener;
import io.keyko.monitoring.agent.core.chain.contract.ContractEventListener;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.container.ChainServicesContainer;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.filter.*;
import io.keyko.monitoring.agent.core.endpoint.response.AbiImportResponse;
import io.keyko.monitoring.agent.core.endpoint.response.AddEventFilterResponse;
import io.keyko.monitoring.agent.core.endpoint.response.AddViewFilterResponse;
import io.keyko.monitoring.agent.core.endpoint.response.FilterEndpointException;
import io.keyko.monitoring.agent.core.integration.broadcast.internal.EventeumEventBroadcaster;
import io.keyko.monitoring.agent.core.model.ViewFilterSubscription;
import io.keyko.monitoring.agent.core.repository.ContractViewFilterRepository;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;
import io.keyko.monitoring.agent.core.utils.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@inheritDoc}
 *
 */
@Slf4j
@Component
public class DefaultAbiImportingService implements AbiImportingService {

    private ViewSubscriptionService viewSubscriptionService;

    private EventSubscriptionService eventSubscriptionService;


    @Autowired
    public DefaultAbiImportingService(ViewSubscriptionService viewSubscriptionService,
                                      EventSubscriptionService eventSubscriptionService) {
        this.viewSubscriptionService = viewSubscriptionService;
        this.eventSubscriptionService = eventSubscriptionService;
    }


    @Override
    public AbiImportResponse importAbi(AbiParser abiParser, String filterType, Integer blockInterval, Integer startBlock) {

        AbiImportResponse importResponse = new AbiImportResponse();

        if (filterType.equals("events") || filterType.equals("all")) {

            List<AddEventFilterResponse> eventsList = new ArrayList<>();
            abiParser.getEvents().stream().forEach( abiEntry -> {
                log.info("Event: " + abiEntry.name);
                final ContractEventFilter eventFilter = buildContractEventFilter(abiParser, abiEntry, startBlock);
                log.info("EVENT-FILTER = " + eventFilter.toString());
                eventSubscriptionService.registerContractEventFilter(eventFilter, true);
                eventsList.add(new AddEventFilterResponse(eventFilter.getId()));
            });
            importResponse.setListEventFilters(eventsList);
        }

        List<AddViewFilterResponse> viewList = new ArrayList<>();
        if (filterType.equals("views") || filterType.equals("all")) {
            abiParser.getViews().stream().forEach(abiEntry -> {
                log.info("View: " + abiEntry.name);
                final ContractViewFilter viewFilter = buildContractViewFilter(abiParser, abiEntry, startBlock);
                viewSubscriptionService.registerContractViewFilter(viewFilter, true);
                viewList.add(new AddViewFilterResponse(viewFilter.getId()));
            });
            importResponse.setListViewFilters(viewList);
        }

        return importResponse;
    }

    public ContractEventFilter buildContractEventFilter(AbiParser abiParser, EthereumAbi.AbiEntry abiEntry, Integer startBlock)   {
        ContractEventFilter eventFilter = new ContractEventFilter();
        eventFilter.setId(buildFilterId("event", abiParser.get().name, abiEntry.name));
        eventFilter.setContractAddress(abiParser.get().address);
        eventFilter.setContractName(abiParser.get().name);
        if (startBlock >0)
            eventFilter.setStartBlock(BigInteger.valueOf(startBlock));

        ContractEventSpecification eventSpecification = new ContractEventSpecification();
        eventSpecification.setContractName(abiEntry.name);
        eventSpecification.setEventName(abiEntry.name);

        List<ParameterDefinition> indexedParameterDefinitions = new ArrayList<>();
        List<ParameterDefinition> nonIndexedParameterDefinitions = new ArrayList<>();
        abiEntry.inputs.forEach( entry -> {
            ParameterDefinition definition = new ParameterDefinition();
            definition.setName(entry.name);
            definition.setType(new ParameterType(entry.type.toUpperCase()));

            if (entry.indexed) {
                definition.setPosition(indexedParameterDefinitions.size());
                indexedParameterDefinitions.add(definition);
            } else {
                definition.setPosition(nonIndexedParameterDefinitions.size());
                nonIndexedParameterDefinitions.add(definition);
            }
        });
        eventSpecification.setIndexedParameterDefinitions(indexedParameterDefinitions);
        eventSpecification.setNonIndexedParameterDefinitions(nonIndexedParameterDefinitions);

        eventFilter.setEventSpecification(eventSpecification);
        return eventFilter;
    }

    public ContractViewFilter buildContractViewFilter(AbiParser abiParser, EthereumAbi.AbiEntry abiEntry, Integer startBlock)   {
        ContractViewFilter viewFilter = new ContractViewFilter();
        viewFilter.setId(buildFilterId("view", abiParser.get().name, abiEntry.name));
        viewFilter.setContractAddress(abiParser.get().address);
        viewFilter.setContractName(abiParser.get().name);
        if (startBlock >0)
            viewFilter.setStartBlock(BigInteger.valueOf(startBlock));

        ContractViewSpecification viewSpecification = new ContractViewSpecification();
        viewSpecification.setContractName(abiEntry.name);
        viewSpecification.setMethodName(abiEntry.name);

        List<MethodParameterDefinition> inputParameterDefinitions = new ArrayList<>();
        abiEntry.inputs.forEach( entry -> {
            MethodParameterDefinition methodDefinition = new MethodParameterDefinition();
            methodDefinition.setName(entry.name);
            methodDefinition.setType(new ParameterType(entry.type.toUpperCase()));
            methodDefinition.setValue(entry.value);
            methodDefinition.setPosition(inputParameterDefinitions.size());
            inputParameterDefinitions.add(methodDefinition);
        });
        viewSpecification.setInputParameterDefinitions(inputParameterDefinitions);

        List<ParameterDefinition> outputParameterDefinitions = new ArrayList<>();
        abiEntry.outputs.forEach( entry -> {
            ParameterDefinition parameterDefinition = new ParameterDefinition();
            parameterDefinition.setPosition(outputParameterDefinitions.size());
            parameterDefinition.setType(new ParameterType(entry.type.toUpperCase()));
            parameterDefinition.setName(entry.name);
            outputParameterDefinitions.add(parameterDefinition);
        });
        viewSpecification.setOutputParameterDefinitions(outputParameterDefinitions);

        return viewFilter;
    }

    private String buildFilterId(String type, String contractName, String functionName) {
        return type.toLowerCase() + "_"
                + contractName + "_" +
                functionName;
    }

}
