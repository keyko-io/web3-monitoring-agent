package io.keyko.monitoring.agent.core.dto.message;

import io.keyko.monitoring.agent.core.dto.log.LogDetails;
import lombok.NoArgsConstructor;

@NoArgsConstructor

public class Log extends AbstractMessage<LogDetails> {

    public static final String TYPE = "LOG_EVENT";

    public Log(LogDetails details) {
        super(details.getId(), TYPE, details);
    }
}
