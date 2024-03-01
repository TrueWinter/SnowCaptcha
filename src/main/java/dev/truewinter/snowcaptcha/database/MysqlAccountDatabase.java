package dev.truewinter.snowcaptcha.database;

import com.zaxxer.hikari.HikariDataSource;
import dev.truewinter.snowcaptcha.admin.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MysqlAccountDatabase extends MysqlDatabase {
    private final HikariDataSource ds;

    public MysqlAccountDatabase(HikariDataSource ds) {
        this.ds = ds;
    }

    public Optional<Account> getAccount(String username) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE username = ?;");
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Account account = new Account(
                        rs.getString("username"),
                        rs.getString("password")
                );
                return Optional.of(account);
            } else {
                return Optional.empty();
            }
        }
    }

    public List<Account> getAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users`;");
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Account account = new Account(
                        rs.getString("username"),
                        rs.getString("password")
                );
                accounts.add(account);
            }
        }

        return accounts;
    }

    public void addAccount(String username, String password) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection
                    .prepareStatement("INSERT INTO `users` (username, password) VALUES (?, ?);");
            statement.setString(1, username);
            statement.setString(2, Account.createHash(password));
            statement.execute();
        }
    }

    public void deleteAccount(String username) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `users` WHERE username = ?");
            statement.setString(1, username);
            statement.execute();
        }
    }

    public void changePassword(String username, String password) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection
                    .prepareStatement("UPDATE users SET password = ? WHERE username = ?");
            statement.setString(1, Account.createHash(password));
            statement.setString(2, username);
            statement.execute();
        }
    }

    @Override
    public String tableName() {
        return "users";
    }

    @Override
    public String[] createTableInnerStatement() {
        return new String[]{
                "`username` varchar(20) NOT NULL PRIMARY KEY",
                "`password` char(60) NOT NULL"
        };
    }

    @Override
    public void runAfterTableCreation() throws SQLException {
        if (getAccounts().isEmpty()) {
            addAccount("admin", "snowcaptcha");
        }
    }
}
