package com.github.hcsp;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    void insertNews2Db(String title, String content, String link) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertProcessedLink(String link);

    void insertLinkToBeProcessed(String link);
}
