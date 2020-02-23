package io.keyko.monitoring.agent.core.dto.event.parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.util.ArrayList;

/**
 * An array EventParameter, backed by an ArrayList.
 * Its ArrayList rather than List as ArrayList implements Serializable.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@Embeddable
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class ArrayParameter<T extends EventParameter<?>> extends AbstractEventParameter<ArrayList<T>> {

    @JsonIgnore
    private String stringRepresentation;

    public ArrayParameter(String entryType, Class<T> arrayParameterType, ArrayList<T> value, String name) {
        super(entryType + "[]", value, name);

        initStringRepresentation();
    }

    @Override
    public String getValueString() {
        return stringRepresentation;
    }

    @Override
    public long getNumberValue() {
        return 0l;
    }

    private void initStringRepresentation() {
        final StringBuilder builder = new StringBuilder("[");

        getValue().forEach(param -> {
            builder.append("\"");
            builder.append(param.getValueString());
            builder.append("\"");
            builder.append(",");
        });

        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");

        stringRepresentation = builder.toString();
    }
}
