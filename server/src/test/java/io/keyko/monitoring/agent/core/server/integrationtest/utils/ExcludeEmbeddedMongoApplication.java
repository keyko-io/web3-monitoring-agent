package io.keyko.monitoring.agent.core.server.integrationtest.utils;

import org.springframework.boot.SpringApplication;

//@SpringBootApplication(exclude = {EmbeddedMongoAutoConfiguration.class})
//@EnableEventeum
public class ExcludeEmbeddedMongoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcludeEmbeddedMongoApplication.class, args);
    }
}