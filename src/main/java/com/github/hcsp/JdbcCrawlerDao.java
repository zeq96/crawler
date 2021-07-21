package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String H2_CONNECTION = "jdbc:h2:file:C:\\WorkSpace\\HCSP\\Thread\\crawler\\news";
    private static final String H2_USERNAME = "root";
    private static final String H2_PASSWORD = "root";

    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection(H2_CONNECTION, H2_USERNAME, H2_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNextLinkThenDelete() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from LINK_TO_BE_PROCESSED limit 1"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String link = resultSet.getString(1);
                if (link != null) {
                    updateLink2Db(link, "delete from LINK_TO_BE_PROCESSED where link = ?");
                    return link;
                }
            }
        }
        return null;
    }

    private void updateLink2Db(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertNews2Db(String title, String content, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into NEWS (title, content, url, created_at, modified_at) values (?, ?, ?, now(), now())")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, link);
            statement.executeUpdate();
        }
    }

    public boolean isLinkProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINK_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    @Override
    public void insertProcessedLink(String link) {

    }

    @Override
    public void insertLinkToBeProcessed(String elementLink) {

    }

}
