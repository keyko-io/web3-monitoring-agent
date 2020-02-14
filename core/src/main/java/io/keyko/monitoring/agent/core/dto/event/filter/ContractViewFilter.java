package io.keyko.monitoring.agent.core.dto.event.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.keyko.monitoring.agent.core.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigInteger;

/**
 * Represents the details of a contract view filter.
 *
 */
@Document
@Entity
@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractViewFilter {

    @Id
    private String id;

    private String contractAddress;

    private String contractName;

    private String node = Constants.DEFAULT_NODE_NAME;

    @Embedded
    private ContractViewSpecification methodSpecification;

    @Embedded
    private PollingStrategySpecification pollingStrategy;

    private BigInteger startBlock;
}
