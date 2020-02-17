package io.keyko.monitoring.agent.core.chain.config;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.event.filter.correlationId.CorrelationIdStrategy;
import io.keyko.monitoring.agent.core.dto.event.filter.correlationId.IndexedParameterCorrelationIdStrategy;
import io.keyko.monitoring.agent.core.dto.event.filter.correlationId.NonIndexedParameterCorrelationIdStrategy;
import io.keyko.monitoring.agent.core.utils.ModelMapperFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Keys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Event Filter Configuration
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@Configuration
@ConfigurationProperties
@Data
public class EventFilterConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EventFilterConfiguration.class);

    private List<EventFilterConfig> eventFilters;

    //Can't seem to be able to unmarshall from config to an interface using Spring.
    //This method converts CorrelationId to CorrelationId strategy
    public List<ContractEventFilter> getConfiguredEventFilters() {
        List<ContractEventFilter> filtersToReturn = new ArrayList<>();

        if (eventFilters != null) {
            final ModelMapper mapper = ModelMapperFactory.getInstance().createModelMapper();

            eventFilters.forEach((configFilter) -> {
                final ContractEventFilter contractEventFilter = new ContractEventFilter();
                mapper.map(configFilter, contractEventFilter);
                contractEventFilter.setContractAddress(Keys.toChecksumAddress(contractEventFilter.getContractAddress()));
                contractEventFilter.setCorrelationIdStrategy(configFilter.getCorrelationId().toStrategy());
                contractEventFilter.setContractAddress(Keys.toChecksumAddress(contractEventFilter.getContractAddress()));
                filtersToReturn.add(contractEventFilter);
            });
        }

        return filtersToReturn;
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    public static class EventFilterConfig extends ContractEventFilter {
        private CorrelationId correlationId;
    }

    @Data
    public static class CorrelationId {
        private String type;

        private int index;

        private Map<String, Callable<CorrelationIdStrategy>> strategyCreatorMap = new HashMap<>();

        public CorrelationId() {
            strategyCreatorMap.put(IndexedParameterCorrelationIdStrategy.TYPE, () -> {
                final IndexedParameterCorrelationIdStrategy indexed = new IndexedParameterCorrelationIdStrategy(index);

                return indexed;
            });

            strategyCreatorMap.put(NonIndexedParameterCorrelationIdStrategy.TYPE, () -> {
                final NonIndexedParameterCorrelationIdStrategy nonIndexed = new NonIndexedParameterCorrelationIdStrategy(index);

                return nonIndexed;
            });
        }

        public CorrelationIdStrategy toStrategy() {
            try {
                return strategyCreatorMap.get(type).call();
            } catch (Exception e) {
                LOG.error("Error when obtaining correlation id strategy...application.yml probably incorrect", e);
                return null;
            }
        }
    }

}
