package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;

public class MysqlConfig {
    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;

    public MysqlConfig(Section section) {
        this.host = section.getString("host");
        this.port = section.getInt("port");
        this.database = section.getString("database");
        this.user = section.getString("user");
        this.password = section.getString("password");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
