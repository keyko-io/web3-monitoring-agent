package io.keyko.monitoring.agent.core.dto.parameter;

import io.keyko.monitoring.agent.core.dto.parameter.AbstractParameter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.math.BigInteger;

/**
 * A number based EventParameter, represented by a BigInteger.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Embeddable
@Data
@NoArgsConstructor
public class NumberParameter extends AbstractParameter<BigInteger> {

    public NumberParameter(String type, BigInteger value, String name) {
        super(type, value, name);
    }

    @Override
    public String getValueString() {
        return getValue().toString();
    }
}
