package io.keyko.monitoring.agent.core.utils;

import java.math.BigInteger;
import java.time.Instant;

public abstract class AvroUtils {

    /**
     * Given a BigInteger return the epoch in milliseconds
     * @param input
     * @return
     */
    public static long toLogicalTypeTimestamp(BigInteger input) {
        Instant millis = Instant.ofEpochMilli(input.longValue());
        return millis.toEpochMilli();
    }

    /**
     * Given a BigInteger the function truncate to a long returning the higher order numbers
     * @param source biginteger number to truncate
     * @return long truncated
     */
    public static long truncateToLong(BigInteger source)    {
        try {
            return source.longValueExact();
        } catch (ArithmeticException ex)    {
            // BigInteger doesn't fit in a long
        }

        try {
            return Long.valueOf(
                    source.toString().substring(0,  String.valueOf(Long.MAX_VALUE).length())).longValue();
        } catch (NumberFormatException ex)    {
            // BigInteger doesn't fit in a long
            return Long.valueOf(
                    source.toString().substring(0,  String.valueOf(Long.MAX_VALUE).length() -1)).longValue();
        }
    }

    /**
     * Given a String the function truncate to a long returning the higher order numbers.
     * If the string includes any non-digit character, these characters are removed
     * @param source string with a number to truncate
     * @return long truncated
     */
    public static long truncateToLong(String source) {
        return truncateToLong(
                new BigInteger( source.replaceAll("[^\\d.]", "")));
    }
}
