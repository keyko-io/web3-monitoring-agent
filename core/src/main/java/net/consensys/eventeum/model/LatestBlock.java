package net.consensys.eventeum.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.consensys.eventeum.BlockDetails;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;
import java.math.BigInteger;

@Document
@Entity
@Data
@NoArgsConstructor
public class LatestBlock {

    public LatestBlock(BlockDetails blockDetails) {
        this.nodeName = blockDetails.getNodeName();
        this.number = new BigInteger(blockDetails.getNumber());
        this.hash = blockDetails.getHash();
        this.timestamp = new BigInteger(blockDetails.getTimestamp());
    }

    @javax.persistence.Id
    @Id
    private String nodeName;

    private BigInteger number;

    private String hash;

    private BigInteger timestamp;
}
