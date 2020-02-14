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
    public Function getWeb3Function()    {
        List<Type> funcInput= new ArrayList<>();
        List<TypeReference<?>> funcOutput = new ArrayList<>();

        inputParameterDefinitions.forEach(definition -> {
            try {
                funcInput.add(definition.getWeb3Type());
            } catch (UnsupportedEncodingException e) {
                log.error("Unable to convert input parameter: " + e.getMessage());
            }
        });

        outputParameterDefinitions.forEach(definition -> {
            try {
                TypeReference typeReference = definition.getWeb3TypeReference();
                funcOutput.add(typeReference);
//                funcOutput.add(definition.getWeb3TypeReference());
            } catch (UnsupportedEncodingException e) {
                log.error("Unable to convert output parameter: " + e.getMessage());
            }
        });

        Function function = new Function(methodName, funcInput, funcOutput);
        return function;
    }
}
