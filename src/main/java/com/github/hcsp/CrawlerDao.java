package com.github.hcsp;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    void updateLink2Db(String link, String sql) throws SQLException;

    void insertNews2Db(String title, String content, String link) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;
}
