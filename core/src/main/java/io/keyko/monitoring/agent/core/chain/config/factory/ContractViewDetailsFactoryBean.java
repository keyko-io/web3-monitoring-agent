package io.keyko.monitoring.agent.core.chain.config.factory;

import io.keyko.monitoring.agent.core.chain.converter.EventParameterConverter;
import io.keyko.monitoring.agent.core.chain.factory.ContractViewDetailsFactory;
import io.keyko.monitoring.agent.core.chain.factory.DefaultContractViewDetailsFactory;
import io.keyko.monitoring.agent.core.chain.settings.Node;
import lombok.Data;
import org.springframework.beans.factory.FactoryBean;
import org.web3j.abi.datatypes.Type;

@Data
public class ContractViewDetailsFactoryBean
        implements FactoryBean<ContractViewDetailsFactory> {

    EventParameterConverter<Type> parameterConverter;
    Node node;
    String nodeName;

    @Override
    public ContractViewDetailsFactory getObject() throws Exception {
        return new DefaultContractViewDetailsFactory(
                parameterConverter, node, nodeName);
    }

    @Override
    public Class<?> getObjectType() {
        return ContractViewDetailsFactory.class;
    }
}
