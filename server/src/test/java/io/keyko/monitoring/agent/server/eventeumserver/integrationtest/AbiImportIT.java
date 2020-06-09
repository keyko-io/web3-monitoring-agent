package io.keyko.monitoring.agent.server.eventeumserver.integrationtest;

import io.keyko.common.helpers.AbiParser;
import io.keyko.monitoring.agent.core.endpoint.AbiImporterEndpoint;
import io.keyko.monitoring.agent.core.endpoint.ContractEventFilterEndpoint;
import io.keyko.monitoring.agent.core.endpoint.response.AbiImportResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AbiImportIT {

    @Autowired
    AbiImporterEndpoint importerEndpoint;

    @Autowired
    ContractEventFilterEndpoint eventFilterEndpoint;

    @Test
    public void abiImport() throws IOException {

        final AbiParser abiParser = AbiParser.loadFromFile("src/test/resources/artifacts/DIDRegistry.json");
        abiParser.get().toJson();

        final AbiImportResponse importedResponse = importerEndpoint.importAbiFilters(
                abiParser.get().toJson(), "events", 500, 0);

        assertEquals(7, importedResponse.getListEventFilters().size());

        importedResponse.getListEventFilters().forEach( eventFilterResponse -> {
            assertEquals(
                    eventFilterEndpoint.getEventFilter(
                            eventFilterResponse.getId(), new MockHttpServletResponse()).getId(), eventFilterResponse.getId());//
        });
    }
}
