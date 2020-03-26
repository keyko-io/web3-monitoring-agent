package io.keyko.monitoring.agent.core.integration.broadcast.blockchain;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.dto.event.parameter.EventParameter;
import io.keyko.monitoring.agent.core.dto.log.LogDetails;
import io.keyko.monitoring.agent.core.dto.message.*;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.integration.KafkaSettings;
import io.keyko.monitoring.agent.core.utils.AvroUtils;
import io.keyko.monitoring.agent.core.utils.JSON;
import io.keyko.monitoring.schemas.*;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A BlockchainEventBroadcaster that broadcasts the events to a Kafka queue.
 * <p>
 * The key for each message will defined by the correlationIdStrategy if configured,
 * or a combination of the transactionHash, blockHash and logIndex otherwise.
 * <p>
 * The topic names for block and contract events can be configured via the
 * kafka.topic.contractEvents and kafka.topic.blockEvents properties.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
public class KafkaBlockchainEventBroadcaster implements BlockchainEventBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBlockchainEventBroadcaster.class);

    private KafkaTemplate<String, GenericRecord> kafkaTemplate;

    private KafkaSettings kafkaSettings;

    private CrudRepository<ContractEventFilter, String> filterEventRepository;

    private CrudRepository<ContractViewFilter, String> filterViewRepository;


    public KafkaBlockchainEventBroadcaster(KafkaTemplate<String, GenericRecord> kafkaTemplate,
                                           KafkaSettings kafkaSettings,
                                           CrudRepository<ContractEventFilter, String> filterEventRepository,
                                           CrudRepository<ContractViewFilter, String> filterViewRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaSettings = kafkaSettings;
        this.filterEventRepository = filterEventRepository;
        this.filterViewRepository = filterViewRepository;
    }

    @Override
    public void broadcastNewBlock(io.keyko.monitoring.agent.core.dto.block.BlockDetails block) {
        final EventeumMessage<io.keyko.monitoring.agent.core.dto.block.BlockDetails> message = createBlockEventMessage(block);
        LOG.info("Sending block message: " + JSON.stringify(message));
        GenericRecord genericRecord = new GenericData.Record(io.keyko.monitoring.schemas.BlockRecord.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("number", message.getDetails().getNumber().longValue());
        genericRecord.put("hash", message.getDetails().getHash());
        genericRecord.put("timestamp",AvroUtils.toLogicalTypeTimestamp(message.getDetails().getTimestamp()));
        genericRecord.put("nodeName", message.getDetails().getNodeName());
        genericRecord.put("retries", message.getRetries());


        kafkaTemplate.send(kafkaSettings.getBlockEventsTopic(), message.getId(), genericRecord);
    }

    @Override
    public void broadcastContractEvent(io.keyko.monitoring.agent.core.dto.event.ContractEventDetails eventDetails) {
        final EventeumMessage<io.keyko.monitoring.agent.core.dto.event.ContractEventDetails> message = createContractEventMessage(eventDetails);
        LOG.info("Sending contract event message: " + JSON.stringify(message));
        GenericRecord genericRecord = new GenericData.Record(io.keyko.monitoring.schemas.EventRecord.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("name",message.getDetails().getName());
        genericRecord.put("contractName",message.getDetails().getContractName());
        genericRecord.put("filterId", message.getDetails().getFilterId());
        genericRecord.put("nodeName", message.getDetails().getNodeName());
        genericRecord.put("indexedParameters", convertParameters(message.getDetails().getIndexedParameters()));
        genericRecord.put("nonIndexedParameters", convertParameters(message.getDetails().getNonIndexedParameters()));
        genericRecord.put("transactionHash", message.getDetails().getTransactionHash());
        genericRecord.put("logIndex", message.getDetails().getLogIndex().toString());
        genericRecord.put("blockNumber", message.getDetails().getBlockNumber().longValue());
        genericRecord.put("blockHash", message.getDetails().getBlockHash());
        genericRecord.put("address", message.getDetails().getAddress());
        genericRecord.put("status",ContractEventStatus.valueOf(message.getDetails().getStatus().name()));
        genericRecord.put("eventSpecificationSignature", message.getDetails().getEventSpecificationSignature());
        genericRecord.put("networkName", message.getDetails().getNetworkName());
        genericRecord.put("retries", message.getRetries());
        kafkaTemplate.send(kafkaSettings.getContractEventsTopic(), message.getId(), genericRecord);
    }

    @Override
    public void broadcastContractView(io.keyko.monitoring.agent.core.dto.view.ContractViewDetails viewDetails) {
        final EventeumMessage<io.keyko.monitoring.agent.core.dto.view.ContractViewDetails> message = createContractViewMessage(viewDetails);
        LOG.info("Sending contract view message: " + JSON.stringify(message));
        GenericRecord genericRecord = new GenericData.Record(io.keyko.monitoring.schemas.ViewRecord.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("name", message.getDetails().getName());
        genericRecord.put("contractName", message.getDetails().getContractName());
        genericRecord.put("filterId", message.getDetails().getFilterId());
        genericRecord.put("nodeName", message.getDetails().getNodeName());
        genericRecord.put("output", convertParameters(message.getDetails().getOutput()));
        genericRecord.put("blockNumber", message.getDetails().getBlockNumber().longValue());
        genericRecord.put("blockHash", message.getDetails().getBlockHash());
        genericRecord.put("address", message.getDetails().getAddress());
        genericRecord.put("networkName", message.getDetails().getNetworkName());
        genericRecord.put("retries", message.getRetries());
        kafkaTemplate.send(kafkaSettings.getContractViewsTopic(), message.getId(), genericRecord);

    }

    @Override
    public void broadcastTransaction(TransactionDetails transactionDetails) {
        final EventeumMessage<io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails> message = createTransactionEventMessage(transactionDetails);
        LOG.info("Sending transaction event message: " + JSON.stringify(message));
        GenericRecord genericRecord = new GenericData.Record(io.keyko.monitoring.schemas.TransactionRecord.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("hash", message.getDetails().getHash());
        genericRecord.put("nonce", message.getDetails().getNonce());
        genericRecord.put("blockNumber", message.getDetails().getBlockNumber().longValue());
        genericRecord.put("blockHash", message.getDetails().getBlockHash());
        genericRecord.put("transactionIndex", message.getDetails().getTransactionIndex());
        genericRecord.put("from", message.getDetails().getFrom());
        genericRecord.put("to", message.getDetails().getTo());
        genericRecord.put("value", message.getDetails().getValue());
        genericRecord.put("nodeName", message.getDetails().getNodeName());
        genericRecord.put("contractAddress", message.getDetails().getContractAddress());
        genericRecord.put("input", message.getDetails().getInput());
        genericRecord.put("revertReason", message.getDetails().getRevertReason());
        genericRecord.put("status", TransactionStatus.valueOf(message.getDetails().getStatus().name()));
        genericRecord.put("retries", message.getRetries());
        kafkaTemplate.send(kafkaSettings.getTransactionEventsTopic(), message.getId(), genericRecord);
    }

    @Override
    public void broadcastLog(LogDetails logDetails) {
        final EventeumMessage<io.keyko.monitoring.agent.core.dto.log.LogDetails> message = createLogMessage(logDetails);
        LOG.info("Sending log message: " + JSON.stringify(message));
        GenericRecord genericRecord = new GenericData.Record(io.keyko.monitoring.schemas.LogRecord.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("nodeName", message.getDetails().getNodeName());
        genericRecord.put("data",message.getDetails().getData());
        genericRecord.put("topics", message.getDetails().getTopics());
        genericRecord.put("transactionHash", message.getDetails().getTransactionHash());
        genericRecord.put("logIndex", message.getDetails().getLogIndex().toString());
        genericRecord.put("blockNumber", message.getDetails().getBlockNumber().longValue());
        genericRecord.put("blockHash", message.getDetails().getBlockHash());
        genericRecord.put("address", message.getDetails().getAddress());
        genericRecord.put("networkName", message.getDetails().getNetworkName());
        genericRecord.put("status", LogStatus.valueOf(message.getDetails().getStatus().name()));
        genericRecord.put("retries", message.getRetries());
        kafkaTemplate.send(kafkaSettings.getLogsTopic(), message.getId(), genericRecord);
    }

    protected EventeumMessage<io.keyko.monitoring.agent.core.dto.block.BlockDetails> createBlockEventMessage(io.keyko.monitoring.agent.core.dto.block.BlockDetails blockDetails) {
        return new BlockEvent(blockDetails);
    }

    protected EventeumMessage<io.keyko.monitoring.agent.core.dto.event.ContractEventDetails> createContractEventMessage(io.keyko.monitoring.agent.core.dto.event.ContractEventDetails contractEventDetails) {
        return new ContractEvent(contractEventDetails);
    }

    protected EventeumMessage<io.keyko.monitoring.agent.core.dto.view.ContractViewDetails> createContractViewMessage(io.keyko.monitoring.agent.core.dto.view.ContractViewDetails contractViewDetails) {
        return new ContractView(contractViewDetails);
    }

    protected EventeumMessage<TransactionDetails> createTransactionEventMessage(TransactionDetails transactionDetails) {
        return new TransactionEvent(transactionDetails);
    }

    protected EventeumMessage<io.keyko.monitoring.agent.core.dto.log.LogDetails> createLogMessage(io.keyko.monitoring.agent.core.dto.log.LogDetails logDetails) {
        return new Log(logDetails);
    }


    public List<Object> convertTopics(List<String> topics) {
        List<Object> topicsConverted = new ArrayList<Object>();
        for (int i = 0; i < topics.size(); i++) {
            topicsConverted.add(topics.get(i));
        }
        return topicsConverted;
    }

    public List<Object> convertParameters(List<EventParameter> l) {
        List<Object> parametersConverted = new ArrayList<Object>();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getClass() == io.keyko.monitoring.agent.core.dto.event.parameter.StringParameter.class) {
                parametersConverted.add(
                        new StringParameter(
                                l.get(i).getName(),
                                l.get(i).getType(),
                                l.get(i).getValueString()
                        ));
            } else if (l.get(i).getClass() == io.keyko.monitoring.agent.core.dto.event.parameter.NumberParameter.class) {
                parametersConverted.add(
                        new NumberParameter(
                                l.get(i).getName(),
                                l.get(i).getType(),
                                l.get(i).getValueString(),
                                AvroUtils.truncateToLong(l.get(i).getValueString())
                        ));
            }
        }
        return parametersConverted;
    }

    private String getContractEventCorrelationId(EventeumMessage<io.keyko.monitoring.agent.core.dto.event.ContractEventDetails> message) {
        final Optional<ContractEventFilter> filter = filterEventRepository.findById(message.getDetails().getFilterId());

        if (!filter.isPresent() || filter.get().getCorrelationIdStrategy() == null) {
            return message.getId();
        }

        return filter
                .get()
                .getCorrelationIdStrategy()
                .getCorrelationId(message.getDetails());
    }

    private String getContractViewCorrelationId(EventeumMessage<io.keyko.monitoring.agent.core.dto.view.ContractViewDetails> message) {
        final Optional<ContractViewFilter> filter = filterViewRepository.findById(message.getDetails().getFilterId());

        if (!filter.isPresent()) {
            return message.getId();
        }

        return filter
                .get()
                .getId();
    }
}
