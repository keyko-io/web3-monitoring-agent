package io.keyko.monitoring.agent.core.factory;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import org.springframework.data.repository.CrudRepository;

public interface ContractViewFilterRepositoryFactory {

    CrudRepository<ContractViewFilter, String> build();
}
