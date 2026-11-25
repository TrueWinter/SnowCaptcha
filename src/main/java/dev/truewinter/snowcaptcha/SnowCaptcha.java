package dev.truewinter.snowcaptcha;

import dev.truewinter.snowcaptcha.config.Config;
import dev.truewinter.snowcaptcha.config.ReputationConfig;
import dev.truewinter.snowcaptcha.database.Mysql;
import dev.truewinter.snowcaptcha.database.Redis;
import dev.truewinter.snowcaptcha.storage.S3Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class SnowCaptcha {
    private static Logger logger;
    private static DebugLogger debugLogger;
    private static Redis redis;
    private static Mysql mysql;
    private static S3Storage s3;
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

        if (config.getS3Config().isConfigured()) {
            s3 = new S3Storage(config.getS3Config());
        } else if (config.getVisualChallengeConfig().isRegionEnabled()) {
            logger.warn("Region challenges are enabled but S3 is not configured, falling back to text challenges");
        }

        redis = new Redis(config.getRedisConfig());
        mysql = new Mysql(config.getMysqlConfig());

        WebServer webServer = new WebServer(config.getHttpConfig(), config.getDevModeConfig());
        webServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            webServer.shutdown();
            mysql.close();
            if (s3 != null) {
                s3.close();
            }
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

    public static S3Storage getS3() {
        return s3;
    }

    public static Config getConfig() {
        return config;
    }

    public static ReputationConfig getReputationConfig() {
        return config.getReputationConfig();
    }

    public static String getSecret() {
        return config.getHttpConfig().getSecret();
    }
}
