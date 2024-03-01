package dev.truewinter.snowcaptcha;

import dev.truewinter.snowcaptcha.config.Config;
import dev.truewinter.snowcaptcha.config.ReputationConfig;
import dev.truewinter.snowcaptcha.database.Mysql;
import dev.truewinter.snowcaptcha.database.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class SnowCaptcha {
    private static Logger logger;
    private static DebugLogger debugLogger;
    private static Redis redis;
    private static Mysql mysql;
    private static Config config = null;

    public static void main(String[] args) {
        logger = LoggerFactory.getLogger(SnowCaptcha.class);

        try {
            config = new Config(Path.of(Util.getInstallPath(), "config.yml").toFile());
        } catch(Exception e) {
            logger.error("Failed to load config", e);
            System.exit(1);
        }

        debugLogger = new DebugLogger(logger, config.isDebugLoggerEnabled());

        redis = new Redis(config.getRedisConfig());
        mysql = new Mysql(config.getMysqlConfig());

        WebServer webServer = new WebServer(config.getHttpConfig(), config.getDevModeConfig());
        webServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            webServer.shutdown();
            mysql.close();
        }));
    }

    public static Logger getLogger() {
        return logger;
    }

    public static DebugLogger getDebugLogger() {
        return debugLogger;
    }

    public static Redis getRedis() {
        return redis;
    }

    public static Mysql getMysql() {
        return mysql;
    }

    public static ReputationConfig getReputationConfig() {
        return config.getReputationConfig();
    }

    public static String getSecret() {
        return config.getHttpConfig().getSecret();
    }
}
