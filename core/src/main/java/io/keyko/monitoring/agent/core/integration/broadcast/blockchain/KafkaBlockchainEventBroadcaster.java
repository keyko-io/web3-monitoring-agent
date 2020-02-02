package io.keyko.monitoring.agent.core.integration.broadcast.blockchain;

import io.keyko.monitoring.agent.core.dto.event.parameter.NumberParameter;
import io.keyko.monitoring.agent.core.dto.event.parameter.StringParameter;
import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.event.parameter.EventParameter;
import io.keyko.monitoring.agent.core.dto.message.BlockEvent;
import io.keyko.monitoring.agent.core.dto.message.ContractEvent;
import io.keyko.monitoring.agent.core.dto.message.EventeumMessage;
import io.keyko.monitoring.agent.core.dto.message.TransactionEvent;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.integration.KafkaSettings;
import io.keyko.monitoring.agent.core.utils.JSON;
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
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class KafkaBlockchainEventBroadcaster implements BlockchainEventBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBlockchainEventBroadcaster.class);

    private KafkaTemplate<String, GenericRecord> kafkaTemplate;

    private KafkaSettings kafkaSettings;

    private CrudRepository<ContractEventFilter, String> filterRespository;

    public KafkaBlockchainEventBroadcaster(KafkaTemplate<String, GenericRecord> kafkaTemplate,
                                           KafkaSettings kafkaSettings,
                                           CrudRepository<ContractEventFilter, String> filterRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaSettings = kafkaSettings;
        this.filterRespository = filterRepository;
    }

    @Override
    public void broadcastNewBlock(BlockDetails block) {
        final EventeumMessage<BlockDetails> message = createBlockEventMessage(block);
        LOG.info("Sending block message: " + JSON.stringify(message));

        io.keyko.monitoring.model.BlockDetails blockDetails = io.keyko.monitoring.model.BlockDetails.newBuilder()
                .setHash(message.getDetails().getHash()).setNodeName(message.getDetails().getNodeName())
                .setNumber(message.getDetails().getNumber().toString()).setTimestamp(message.getDetails().getTimestamp().toString()).build();

        GenericRecord genericRecord = new GenericData.Record(io.keyko.monitoring.model.BlockEvent.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("type", message.getType());
        genericRecord.put("details", blockDetails);
        genericRecord.put("retries", message.getRetries());


        kafkaTemplate.send(kafkaSettings.getBlockEventsTopic(), message.getId(), genericRecord);
    }

    @Override
    public void broadcastContractEvent(ContractEventDetails eventDetails) {
        final EventeumMessage<ContractEventDetails> message = createContractEventMessage(eventDetails);
        LOG.info("Sending contract event message: " + JSON.stringify(message));
        io.keyko.monitoring.model.ContractEventDetails contractEventDetails = io.keyko.monitoring.model.ContractEventDetails.newBuilder()
                .setAddress(message.getDetails().getAddress()).setBlockHash(message.getDetails().getBlockHash()).setBlockNumber(message.getDetails().getBlockNumber().toString())
                .setEventSpecificationSignature(message.getDetails().getEventSpecificationSignature()).setFilterId(message.getDetails().getFilterId())
                .setId(message.getDetails().getId()).setLogIndex(message.getDetails().getLogIndex().toString())
                .setName(message.getDetails().getName()).setNetworkName(message.getDetails().getNetworkName()).setNodeName(message.getDetails().getNodeName())
                .setNonIndexedParameters(convertParameters(message.getDetails().getNonIndexedParameters())).setIndexedParameters(convertParameters(message.getDetails().getIndexedParameters()))
                .setStatus(io.keyko.monitoring.model.ContractEventStatus.valueOf(message.getDetails().getStatus().name()))
                .setTransactionHash(message.getDetails().getTransactionHash()).build();

        GenericRecord genericRecord = new GenericData.Record(io.keyko.monitoring.model.ContractEvent.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("type", message.getType());
        genericRecord.put("details", contractEventDetails);
        genericRecord.put("retries", message.getRetries());

        kafkaTemplate.send(kafkaSettings.getContractEventsTopic(), getContractEventCorrelationId(message), genericRecord);
    }

    @Override
    public void broadcastTransaction(TransactionDetails transactionDetails) {
        final EventeumMessage<TransactionDetails> message = createTransactionEventMessage(transactionDetails);
        LOG.info("Sending transaction event message: " + JSON.stringify(message));
        GenericRecord genericRecord = new GenericData.Record(io.keyko.monitoring.model.TransactionEvent.getClassSchema());
        genericRecord.put("id", message.getId());
        genericRecord.put("type", message.getType());
        genericRecord.put("details", message.getDetails());
        genericRecord.put("retries", message.getRetries());
        kafkaTemplate.send(kafkaSettings.getTransactionEventsTopic(), transactionDetails.getBlockHash(), genericRecord);
    }

    protected EventeumMessage<BlockDetails> createBlockEventMessage(BlockDetails blockDetails) {
        return new BlockEvent(blockDetails);
    }

    protected EventeumMessage<ContractEventDetails> createContractEventMessage(ContractEventDetails contractEventDetails) {
        return new ContractEvent(contractEventDetails);
    }

    protected EventeumMessage<TransactionDetails> createTransactionEventMessage(TransactionDetails transactionDetails) {
        return new TransactionEvent(transactionDetails);
    }

    public List<Object> convertParameters(List<EventParameter> l) {
        List<Object> parametersConverted = new ArrayList<Object>();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getClass() == StringParameter.class) {
                parametersConverted.add(new StringParameter(l.get(i).getName(), l.get(i).getType(), l.get(i).getValueString()));
            } else if (l.get(i).getClass() == NumberParameter.class) {
                parametersConverted.add(new io.keyko.monitoring.model.NumberParameter(l.get(i).getName(), l.get(i).getType(), l.get(i).getValueString()));
            }
        }
        return parametersConverted;
    }

    private String getContractEventCorrelationId(EventeumMessage<ContractEventDetails> message) {
        final Optional<ContractEventFilter> filter = filterRespository.findById(message.getDetails().getFilterId());

        if (!filter.isPresent() || filter.get().getCorrelationIdStrategy() == null) {
            return message.getId();
        }

        return filter
                .get()
                .getCorrelationIdStrategy()
                .getCorrelationId(message.getDetails());
    }
}
