package io.keyko.monitoring.agent.kafkadl.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class DummyDetails {
    String stringValue;
    BigInteger bigIntegerValue;
}
