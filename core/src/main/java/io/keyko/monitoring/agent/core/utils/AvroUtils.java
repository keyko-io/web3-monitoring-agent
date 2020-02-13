package io.keyko.monitoring.agent.core.utils;

import java.math.BigInteger;
import java.time.Instant;

public abstract class AvroUtils {

    public static long toLogicalTypeTimestamp(BigInteger input) {
        Instant millis = Instant.ofEpochMilli(input.longValue());
        return millis.toEpochMilli();
    }
}
