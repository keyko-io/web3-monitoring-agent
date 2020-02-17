package io.keyko.monitoring.agent.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keyko.monitoring.agent.core.integration.mixin.PageMixIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

/**
 * Configures the jackson ObjectMapper bean.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Page.class, PageMixIn.class);

        return mapper;
    }
}
