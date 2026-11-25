package dev.truewinter.snowcaptcha.database;

import com.zaxxer.hikari.HikariDataSource;
import dev.truewinter.snowcaptcha.admin.RegionChallenge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MysqlRegionChallengeDatabase extends MysqlDatabase {
    private final HikariDataSource ds;

    public MysqlRegionChallengeDatabase(HikariDataSource ds) {
        this.ds = ds;
    }

    public List<RegionChallenge> getRegionChallenges() throws SQLException {
        List<RegionChallenge> challenges = new ArrayList<>();

        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM region_challenges ORDER BY id DESC;");
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                challenges.add(fromResultSet(rs));
            }
        }

        return challenges;
    }

    public Optional<RegionChallenge> getRegionChallenge(long id) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM region_challenges WHERE id = ?;");
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(fromResultSet(rs));
            }

            return Optional.empty();
        }
    }

    public Optional<RegionChallenge> getRandomRegionChallenge() throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM region_challenges ORDER BY RAND() LIMIT 1;");
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(fromResultSet(rs));
            }

            return Optional.empty();
        }
    }

    public RegionChallenge addRegionChallenge(String objectKey, String contentType, String instructions,
                                              double x, double y, double width, double height) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO region_challenges " +
                            "(object_key, content_type, instructions, `x`, `y`, `width`, `height`) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, objectKey);
            statement.setString(2, contentType);
            statement.setString(3, instructions);
            statement.setDouble(4, x);
            statement.setDouble(5, y);
            statement.setDouble(6, width);
            statement.setDouble(7, height);
            statement.execute();

            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                return new RegionChallenge(keys.getLong(1), objectKey, contentType, instructions,
                        x, y, width, height);
            }

            throw new SQLException("Failed to get generated region challenge id");
        }
    }

    public void updateRegionChallenge(long id, String objectKey, String contentType, String instructions,
                                      double x, double y, double width, double height) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE region_challenges SET object_key = ?, content_type = ?, instructions = ?, " +
                            "`x` = ?, `y` = ?, `width` = ?, `height` = ? WHERE id = ?;");
            statement.setString(1, objectKey);
            statement.setString(2, contentType);
            statement.setString(3, instructions);
            statement.setDouble(4, x);
            statement.setDouble(5, y);
            statement.setDouble(6, width);
            statement.setDouble(7, height);
            statement.setLong(8, id);
            statement.execute();
        }
    }

    public void deleteRegionChallenge(long id) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM region_challenges WHERE id = ?;");
            statement.setLong(1, id);
            statement.execute();
        }
    }

    private RegionChallenge fromResultSet(ResultSet rs) throws SQLException {
        return new RegionChallenge(
                rs.getLong("id"),
                rs.getString("object_key"),
                rs.getString("content_type"),
                rs.getString("instructions"),
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("width"),
                rs.getDouble("height")
        );
    }

    @Override
    public String tableName() {
        return "region_challenges";
    }

    @Override
    public String[] createTableInnerStatement() {
        return new String[]{
                "`id` int NOT NULL AUTO_INCREMENT PRIMARY KEY",
                "`object_key` varchar(255) NOT NULL",
                "`content_type` varchar(100) NOT NULL",
                "`instructions` varchar(255) NOT NULL",
                "`x` double NOT NULL",
                "`y` double NOT NULL",
                "`width` double NOT NULL",
                "`height` double NOT NULL"
        };
    }
}
