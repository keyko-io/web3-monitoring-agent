package io.keyko.monitoring.agent.core.utils;

import io.keyko.monitoring.schemas.BlockDetailsRecord;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AvroUtilsTest {

    @Test
    void toLogicalTypeTimestamp() {
        BigInteger number= BigInteger.valueOf(1581605300l);
        long l = AvroUtils.toLogicalTypeTimestamp(number);
        assertEquals(1581605300l, l);
    }

    @Test
    void toAvroRecordWithTimestamp()    {
        BigInteger number= BigInteger.valueOf(1581605300l);
        BlockDetailsRecord blockDetailsRecord = BlockDetailsRecord.newBuilder()
                .setHash("0x17ef13ce9c048ebd1f4a362f4afb797fe306bfbe5419e5bef254ea287139d031")
                .setNodeName("default")
                .setNumber(578276l)
                .setTimestamp(AvroUtils.toLogicalTypeTimestamp(number))
                .build();

        assertEquals("default", blockDetailsRecord.getNodeName());
        assertEquals(1581605300l, blockDetailsRecord.getTimestamp());
    }
}