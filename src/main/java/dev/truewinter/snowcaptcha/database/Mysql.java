package dev.truewinter.snowcaptcha.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.config.MysqlConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class Mysql {
    private final HikariDataSource ds;
    private final MysqlAccountDatabase mysqlAccountDatabase;
    private final MysqlWidgetDatabase mysqlWidgetDatabase;

    public Mysql(MysqlConfig mysqlConfig) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + mysqlConfig.getHost() +
                ":" + mysqlConfig.getPort() + "/" + mysqlConfig.getDatabase()
                + "?autoReconnect=true");
        hikariConfig.setUsername(mysqlConfig.getUser());
        hikariConfig.setPassword(mysqlConfig.getPassword());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setKeepaliveTime(30000);
        hikariConfig.setConnectionTimeout(20000);

        // Set additional connection pool properties
        hikariConfig.setDataSourceProperties(new Properties() {{
            put("cachePrepStmts", "true");
            put("prepStmtCacheSize", "250");
            put("prepStmtCacheSqlLimit", "2048");
            put("useServerPrepStmts", "true");
            put("useLocalSessionState", "true");
            put("useLocalTransactionState", "true");
            put("rewriteBatchedStatements", "true");
            put("cacheResultSetMetadata", "true");
            put("cacheServerConfiguration", "true");
            put("elideSetAutoCommits", "true");
            put("maintainTimeStats", "false");
        }});

        this.ds = new HikariDataSource(hikariConfig);

        SnowCaptcha.getLogger().info("Connected to MySQL");

        this.mysqlAccountDatabase = new MysqlAccountDatabase(ds);
        this.mysqlWidgetDatabase = new MysqlWidgetDatabase(ds);

        try {
            runSql("SET DEFAULT_STORAGE_ENGINE = INNODB;");
            runSql("SET FOREIGN_KEY_CHECKS = 1;");
            createTablesIfNotExists(mysqlAccountDatabase);
            createTablesIfNotExists(mysqlWidgetDatabase);
        } catch (SQLException e) {
            SnowCaptcha.getLogger().error("Failed to create tables", e);
        }
    }

    public MysqlAccountDatabase getMysqlAccountDatabase() {
        return mysqlAccountDatabase;
    }

    public MysqlWidgetDatabase getMysqlWidgetDatabase() {
        return mysqlWidgetDatabase;
    }

    public void close() {
        ds.close();
    }

    private void runSql(String sql) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            connection.createStatement().execute(sql);
        }
    }

    private void createTablesIfNotExists(MysqlDatabase database) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("CREATE TABLE IF NOT EXISTS `%s` (%n", database.tableName()));

        for (int i = 0; i < database.createTableInnerStatement().length; i++) {
            sb.append(String.format("    %s", database.createTableInnerStatement()[i]));

            if (i != database.createTableInnerStatement().length - 1) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append(") CHARACTER SET utf8 COLLATE utf8_unicode_ci;");
        runSql(sb.toString());

        database.runAfterTableCreation();
    }
}
