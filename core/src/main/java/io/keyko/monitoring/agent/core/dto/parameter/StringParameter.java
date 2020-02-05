package io.keyko.monitoring.agent.core.dto.parameter;

import io.keyko.monitoring.agent.core.dto.parameter.AbstractParameter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

/**
 * A textual based Parameter, represented by a String.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Embeddable
@Data
@NoArgsConstructor
public class StringParameter extends AbstractParameter<String> {

    public StringParameter(String type, String value, String name) {
        super(type, value, name);
    }

    @Override
    public String getValueString() {
        return getValue();
    }
}
