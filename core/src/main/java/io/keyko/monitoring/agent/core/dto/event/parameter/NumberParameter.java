package io.keyko.monitoring.agent.core.dto.event.parameter;

import io.keyko.monitoring.agent.core.utils.AvroUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.math.BigInteger;

/**
 * A number based EventParameter, represented by a BigInteger.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@Embeddable
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class NumberParameter extends AbstractEventParameter<BigInteger> {

    private long numberValue;

    public NumberParameter(String type, BigInteger value, String name) {
        super(type, value, name);
        this.numberValue= AvroUtils.truncateToLong(value);
    }

    @Override
    public String getValueString() {
        return getValue().toString();
    }

    @Override
    public long getNumberValue() {
        return numberValue;
    }


}
