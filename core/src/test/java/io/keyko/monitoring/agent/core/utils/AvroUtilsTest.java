package io.keyko.monitoring.agent.core.utils;

import io.keyko.monitoring.schemas.BlockRecord;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AvroUtilsTest {


    @Test
    void bigIntegerToLong() {

        BigInteger number= new BigInteger(  "3994112015276946430");
        BigInteger tooLarge= new BigInteger("11941120152769464305040549");
        BigInteger maxLong= new BigInteger(String.valueOf(Long.MAX_VALUE));
        BigInteger maxLongPlusOne= new BigInteger(String.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE);

        assertEquals(3994112015276946430l, AvroUtils.truncateToLong(number));
        assertEquals(1194112015276946430l, AvroUtils.truncateToLong(tooLarge));
        assertEquals(Long.MAX_VALUE, AvroUtils.truncateToLong(maxLong));
        assertEquals(922337203685477580l, AvroUtils.truncateToLong(maxLongPlusOne));
//
        assertEquals( AvroUtils.truncateToLong(number),  AvroUtils.truncateToLong(number.toString()));

    }

    @Test
    void toLogicalTypeTimestamp() {
        BigInteger number= BigInteger.valueOf(1581605300l);
        long l = AvroUtils.toLogicalTypeTimestamp(number);
        assertEquals(1581605300l, l);
    }

    @Test
    void toAvroRecordWithTimestamp()    {
        BigInteger number= BigInteger.valueOf(1581605300l);
        BlockRecord blockRecord = BlockRecord.newBuilder()
                .setId("id")
                .setRetries(0)
                .setHash("0x17ef13ce9c048ebd1f4a362f4afb797fe306bfbe5419e5bef254ea287139d031")
                .setNodeName("default")
                .setNumber(578276l)
                .setTimestamp(AvroUtils.toLogicalTypeTimestamp(number))
                .build();

        assertEquals("default", blockRecord.getNodeName());
        assertEquals(1581605300l, blockRecord.getTimestamp());
    }
}