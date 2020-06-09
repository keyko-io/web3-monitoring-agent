package io.keyko.monitoring.agent.core.service;

import io.keyko.common.helpers.AbiParser;
import io.keyko.monitoring.agent.core.endpoint.response.AbiImportResponse;
import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import io.keyko.monitoring.agent.core.service.exception.NotFoundException;

public interface AbiImportingService {

    AbiImportResponse importAbi(AbiParser abiParser, String filterType, Integer blockInterval, Integer startBlock);

}
