package io.keyko.monitoring.agent.core.dto.log;

import io.keyko.monitoring.agent.core.dto.event.ContractEventStatus;
import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Lob;
import java.math.BigInteger;
import java.util.List;


@Data
public class LogDetails {

    private String nodeName;

    @Lob
    // required because of https://stackoverflow.com/questions/43412517/sql-string-or-binary-data-would-be-truncated-error/43426863
    @ElementCollection
    private List<String> topics;

    @Lob
    @ElementCollection
    private String data;

    private String transactionHash;

    private BigInteger logIndex;

    private BigInteger blockNumber;

    private String blockHash;

    private String address;

    private String networkName;

    private ContractEventStatus status = ContractEventStatus.UNCONFIRMED;

    public String getId() {
        return transactionHash + "-" + blockHash + "-" + logIndex;
    }

}
