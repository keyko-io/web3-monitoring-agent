package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.dto.block.BlockDetails;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class DefaultBlockDetailsFactory implements BlockDetailsFactory {

    @Override
    public BlockDetails createBlockDetails(Block block) {
        final BlockDetails blockDetails = new BlockDetails();

        blockDetails.setNumber(block.getNumber());
        blockDetails.setHash(block.getHash());
        blockDetails.setTimestamp(convertTimestampToMilliseconds(block.getTimestamp()));
        blockDetails.setNodeName(block.getNodeName());

        return blockDetails;
    }

    private BigInteger convertTimestampToMilliseconds(BigInteger timestamp) {
        if (timestamp == null) {
            return timestamp;
        }
        return timestamp.multiply(new BigInteger("1000"));
    }

}
