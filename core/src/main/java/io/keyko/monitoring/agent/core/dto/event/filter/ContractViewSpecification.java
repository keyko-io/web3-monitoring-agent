package io.keyko.monitoring.agent.core.dto.event.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents contract view specification, to be used when registering a new filter.
 *
 */
@Embeddable
@Data
@EqualsAndHashCode
@Slf4j
public class ContractViewSpecification implements Serializable {

    private String methodName;

    private String contractName;

    @ElementCollection
    private List<MethodParameterDefinition> inputParameterDefinitions = new ArrayList<>();

    @ElementCollection
    private List<ParameterDefinition> outputParameterDefinitions = new ArrayList<>();

    @Transient
    public Function getWeb3Function() throws UnsupportedEncodingException   {
        List<Type> funcInput= new ArrayList<>();
        List<TypeReference<?>> funcOutput = new ArrayList<>();

        for (MethodParameterDefinition inputParameterDefinition : inputParameterDefinitions) {
            funcInput.add(inputParameterDefinition.getWeb3Type());
        }

        for (ParameterDefinition definition : outputParameterDefinitions) {
            funcOutput.add(definition.getWeb3TypeReference());
        }

        Function function = new Function(methodName, funcInput, funcOutput);
        return function;
    }
}
