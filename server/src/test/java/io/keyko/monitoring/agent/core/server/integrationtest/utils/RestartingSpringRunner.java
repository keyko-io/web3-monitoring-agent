package io.keyko.monitoring.agent.core.server.integrationtest.utils;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class RestartingSpringRunner extends RestartingSpringJUnit4ClassRunner {

    public RestartingSpringRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }
}
