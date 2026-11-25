package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.truewinter.snowcaptcha.Util;

public class S3Config {
    private final String endpoint;
    private final String region;
    private final String bucket;
    private final String accessKey;
    private final String secretKey;
    private final boolean pathStyleAccess;
    private final String keyPrefix;
    private final int healthCheckInterval;

    public S3Config(Section section) {
        this.endpoint = section.getString("endpoint", "");
        this.region = section.getString("region", "us-east-1");
        this.bucket = section.getString("bucket", "");
        this.accessKey = section.getString("access_key", "");
        this.secretKey = section.getString("secret_key", "");
        this.pathStyleAccess = section.getBoolean("path_style_access", true);
        this.keyPrefix = section.getString("key_prefix", "region-challenges/");
        this.healthCheckInterval = section.getInt("health_check_interval", 300);
    }

    public S3Config() {
        this.endpoint = "";
        this.region = "us-east-1";
        this.bucket = "";
        this.accessKey = "";
        this.secretKey = "";
        this.pathStyleAccess = true;
        this.keyPrefix = "region-challenges/";
        this.healthCheckInterval = 300;
    }

    public boolean isConfigured() {
        return !Util.isBlank(bucket) && !Util.isBlank(accessKey) && !Util.isBlank(secretKey);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRegion() {
        return region;
    }

    public String getBucket() {
        return bucket;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public int getHealthCheckInterval() {
        return healthCheckInterval;
    }
}
