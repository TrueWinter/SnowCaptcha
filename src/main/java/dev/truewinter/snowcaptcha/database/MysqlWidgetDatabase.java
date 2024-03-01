package dev.truewinter.snowcaptcha.database;

import com.zaxxer.hikari.HikariDataSource;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.Widget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MysqlWidgetDatabase extends MysqlDatabase {
    private final HikariDataSource ds;

    public MysqlWidgetDatabase(HikariDataSource ds) {
        this.ds = ds;
    }

    public Optional<Widget> getWidget(String sitekey) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM widgets WHERE sitekey = ?;");
            statement.setString(1, sitekey);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Widget widget = new Widget(
                        rs.getString("sitekey"),
                        rs.getString("secretkey"),
                        rs.getString("name"),
                        true
                );

                return Optional.of(widget);
            } else {
                return Optional.empty();
            }
        }
    }

    public List<Widget> getWidgets() throws SQLException {
        List<Widget> widgets = new ArrayList<>();

        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM widgets;");
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Widget widget = new Widget(
                        rs.getString("sitekey"),
                        rs.getString("secretkey"),
                        rs.getString("name"),
                        true
                );

                widgets.add(widget);
            }
        }

        return widgets;
    }

    public Widget addWidget(String name) throws SQLException {
        final String[] keypair = generateKeyPair();
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection
                    .prepareStatement("INSERT INTO widgets (sitekey, secretkey, name) VALUES (?, ?, ?);");
            statement.setString(1, keypair[0]);
            statement.setString(2, Widget.createHash(keypair[1]));
            statement.setString(3, name);
            statement.execute();

            return new Widget(
                    keypair[0],
                    keypair[1],
                    name,
                    false
            );
        }
    }

    public void changeName(String sitekey, String name) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection
                    .prepareStatement("UPDATE widgets SET name = ? WHERE sitekey = ?");
            statement.setString(1, name);
            statement.setString(2, sitekey);
            statement.execute();
        }
    }

    public Widget resetSecretKey(String sitekey) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            final String secretkey = generateKey();

            PreparedStatement statement = connection
                    .prepareStatement("UPDATE widgets SET secretkey = ? WHERE sitekey = ?;");
            statement.setString(1, Widget.createHash(secretkey));
            statement.setString(2, sitekey);
            statement.execute();

            Optional<Widget> widget = getWidget(sitekey);
            if (widget.isEmpty()) {
                throw new IllegalStateException();
            }

            return new Widget(
                    sitekey,
                    secretkey,
                    widget.get().getName(),
                    false
            );
        }
    }

    public void deleteWidget(String sitekey) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `widgets` WHERE sitekey = ?");
            statement.setString(1, sitekey);
            statement.execute();
        }
    }

    private String[] generateKeyPair() throws SQLException {
        String sitekey = generateKey();
        String secretkey = generateKey();

        if (getWidget(sitekey).isPresent()) {
            return generateKeyPair();
        }

        return new String[]{
                sitekey, secretkey
        };
    }

    private String generateKey() {
        return Util.generateRandomString(32);
    }

    @Override
    public String tableName() {
        return "widgets";
    }

    @Override
    public String[] createTableInnerStatement() {
        return new String[]{
                "`sitekey` varchar(64) NOT NULL PRIMARY KEY",
                "`secretkey` char(60) NOT NULL",
                "`name` varchar(40) NOT NULL"
        };
    }

}
