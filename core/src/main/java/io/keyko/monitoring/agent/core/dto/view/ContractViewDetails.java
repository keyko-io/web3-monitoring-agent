package io.keyko.monitoring.agent.core.dto.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.dto.event.ContractEventStatus;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.keyko.monitoring.agent.core.dto.event.parameter.EventParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.web3j.abi.datatypes.Type;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.math.BigInteger;
import java.util.List;

/**
 * Represents the details of an emitted Ethereum smart contract view.
 *
 */
@Document
@Entity
@Data
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractViewDetails {

    private String name;

    private String contractName;

    @Id
    private String filterId;

    private String nodeName;

    @Lob
    @ElementCollection
    private List<EventParameter> output;

    private BigInteger blockNumber;

    private String blockHash;

    private String address;

    private String networkName;

    public String getId() {
        return blockNumber + "-" + blockHash + "-" + name + "-" + address;
    }

}
