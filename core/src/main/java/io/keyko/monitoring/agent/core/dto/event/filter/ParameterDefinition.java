package io.keyko.monitoring.agent.core.dto.event.filter;

import io.keyko.monitoring.agent.core.chain.converter.Web3Converter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.web3j.abi.TypeReference;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDefinition implements Comparable<ParameterDefinition>, Serializable {

    Integer position;

    @Embedded
    ParameterType type;

    String name;

    @Override
    public int compareTo(ParameterDefinition o) {
        return this.position.compareTo(o.getPosition());
    }

    public TypeReference getWeb3TypeReference() throws UnsupportedEncodingException {
        return Web3Converter.getTypeReference(type.getType());
    }

}
