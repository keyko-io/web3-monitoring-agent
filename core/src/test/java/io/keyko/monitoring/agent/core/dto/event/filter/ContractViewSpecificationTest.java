package io.keyko.monitoring.agent.core.dto.event.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.abi.datatypes.Function;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContractViewSpecificationTest {

    private static ContractViewSpecification spec;
    private static final String accountAddress= "0x7c08fEc4dA47EbeCe57DE73204bd632DDAC91027".toLowerCase();

    @BeforeEach
    void setUp() {
        spec= new ContractViewSpecification();
        spec.setMethodName("balanceOf");
        List<MethodParameterDefinition> inputParameterDefinitions = new ArrayList<>();
        List<ParameterDefinition> outputParameterDefinitions = new ArrayList<>();

        MethodParameterDefinition input= new MethodParameterDefinition();
        input.position= 0;
        input.type= new ParameterType("address");
        input.setValue(accountAddress);
        inputParameterDefinitions.add(input);
        spec.setInputParameterDefinitions(inputParameterDefinitions);

        ParameterDefinition output= new ParameterDefinition();
        output.position= 0;
        output.type= new ParameterType("UINT256");
        output.name= "balance";
        outputParameterDefinitions.add(output);
        spec.setOutputParameterDefinitions(outputParameterDefinitions);
    }

    @Test
    void getWeb3Function() {
        Function function= spec.getWeb3Function();
        assertEquals("balanceOf", function.getName());
        assertEquals(1, function.getInputParameters().size());
        assertEquals(1, function.getOutputParameters().size());

        assertEquals(accountAddress, function.getInputParameters().get(0).getValue().toString().toLowerCase());
        assertEquals("address",
                function.getInputParameters().get(0).getTypeAsString());

        assertEquals("org.web3j.abi.datatypes.Uint",
                function.getOutputParameters().get(0).getType().getTypeName());

    }
}