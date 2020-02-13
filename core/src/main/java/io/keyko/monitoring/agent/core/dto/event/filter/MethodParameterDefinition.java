package io.keyko.monitoring.agent.core.dto.event.filter;

import io.keyko.monitoring.agent.core.chain.converter.Web3Converter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodParameterDefinition implements Comparable<MethodParameterDefinition>, Serializable {

    Integer position;

    @Embedded
    ParameterType type;

    String name;

    private String value;

    @Override
    public int compareTo(MethodParameterDefinition o) {
        return this.position.compareTo(o.getPosition());
    }

    public Type getWeb3Type() throws UnsupportedEncodingException {
        return Web3Converter.getEncodeAbiType(type.getType(), value);
    }

}
