package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;

public class RedisConfig {
    private final String host;
    private final int port;
    private final String user;
    private final String password;

    public RedisConfig(Section redis) {
        this.host = redis.getString("host");
        this.port = redis.getInt("port");
        this.user = redis.getString("user");
        this.password = redis.getString("password");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
