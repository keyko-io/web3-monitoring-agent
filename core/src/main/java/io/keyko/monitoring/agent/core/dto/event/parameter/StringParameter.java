package io.keyko.monitoring.agent.core.dto.event.parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

/**
 * A textual based EventParameter, represented by a String.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@Embeddable
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class StringParameter extends AbstractEventParameter<String> {

    public StringParameter(String type, String value, String name) {
        super(type, value, name);
    }

    @Override
    public String getValueString() {
        return getValue();
    }

    @Override
    public long getLongValue() {
        return 0l;
    }
}
