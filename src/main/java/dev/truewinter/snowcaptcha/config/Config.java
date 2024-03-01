package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.truewinter.snowcaptcha.SnowCaptcha;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Config {
    private final HttpConfig httpConfig;
    private final RedisConfig redisConfig;
    private final MysqlConfig mysqlConfig;
    private final ReputationConfig reputationConfig;
    private final boolean debugLoggerEnabled;
    private final DevModeConfig devModeConfig;

    public Config(File file) throws IOException {
        boolean configFileExists = file.exists();

        YamlDocument yamlDocument = YamlDocument.create(file,
                Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")));

        if (!configFileExists) {
            SnowCaptcha.getLogger().info("Copied default config, modify as needed and then start SnowCaptcha again");
            System.exit(1);
        }

        this.httpConfig = new HttpConfig(yamlDocument.getSection("http"));
        this.redisConfig = new RedisConfig(yamlDocument.getSection("redis"));
        this.mysqlConfig = new MysqlConfig(yamlDocument.getSection("mysql"));
        this.reputationConfig = new ReputationConfig(yamlDocument.getSection("reputation"));
        this.debugLoggerEnabled = yamlDocument.getBoolean("debug_logger_enabled", false);

        if (yamlDocument.contains("dev_mode")) {
            this.devModeConfig = new DevModeConfig(yamlDocument.getSection("dev_mode"));
        } else {
            this.devModeConfig = new DevModeConfig();
        }
    }

    public HttpConfig getHttpConfig() {
        return httpConfig;
    }

    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    public MysqlConfig getMysqlConfig() {
        return mysqlConfig;
    }

    public ReputationConfig getReputationConfig() {
        return reputationConfig;
    }

    public boolean isDebugLoggerEnabled() {
        return debugLoggerEnabled;
    }

    public DevModeConfig getDevModeConfig() {
        return devModeConfig;
    }
}
