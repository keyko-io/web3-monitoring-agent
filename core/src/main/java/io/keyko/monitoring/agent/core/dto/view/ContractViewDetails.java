package io.keyko.monitoring.agent.core.dto.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.keyko.monitoring.agent.core.dto.event.ContractEventStatus;
import io.keyko.monitoring.agent.core.dto.event.parameter.EventParameter;
import io.keyko.monitoring.agent.core.dto.view.parameter.ViewParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.math.BigInteger;
import java.util.List;

/**
 * Represents the details of the output generated after a call to a view in a Ethereum smart contract
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

    @Id
    private String viewId;

    private String nodeName;

    @Lob
    @ElementCollection
    private List<ViewParameter> parameters;

    @Lob
    @ElementCollection
    private List<ViewParameter> output;

    private String transactionHash;

    private BigInteger blockNumber;

    private String blockHash;

    private String address;

    private String methodSignature;

    private String networkName;

    public String getId() {
        return transactionHash + "-" + blockHash + "-" + viewId;
    }
}
