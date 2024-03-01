package dev.truewinter.snowcaptcha;

import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

public class DebugLogger {
    private final Logger logger;
    private final boolean enabled;

    public DebugLogger(Logger logger, boolean enabled) {
        this.logger = logger;
        this.enabled = enabled;
    }

    public Logger getLogger() {
        if (!enabled) {
            return NOPLogger.NOP_LOGGER;
        }

        return logger;
    }
}
