package dev.truewinter.snowcaptcha.database;

import java.sql.SQLException;

public abstract class MysqlDatabase {
    public abstract String tableName();
    public abstract String[] createTableInnerStatement();
    public void runAfterTableCreation() throws SQLException {}
}
