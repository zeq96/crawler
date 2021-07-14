package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Main {
    private static final String H2_CONNECTION = "jdbc:h2:file:C:\\WorkSpace\\HCSP\\Thread\\crawler\\news";
    private static final String H2_USERNAME = "root";
    private static final String H2_PASSWORD = "root";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {

        final Connection connection = DriverManager.getConnection(H2_CONNECTION, H2_USERNAME, H2_PASSWORD);

        String link;
        while ((link = getNextLinkThenDelete(connection)) != null) {
            // 已经处理过的不进行处理
            if (isLinkProcessed(connection, link)) {
                continue;
            }
            // 只关心news.sina.cn , 并且过滤登录页面
            if (isInterestedLink(link)) {
                System.out.println(link);
                Document document = httpGetAndParseHtml(link);

                parseUrlsFromPageAndStoreIntoDb(connection, document);

                insertDbIfItsNewsPage(connection, document, link);

                updateLink2Db(connection, link, "insert into LINK_ALREADY_PROCESSED (link) values (?)");
            }
        }

    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from LINK_TO_BE_PROCESSED limit 1"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String link = resultSet.getString(1);
                if (link != null) {
                    updateLink2Db(connection, link, "delete from LINK_TO_BE_PROCESSED where link = ?");
                    return link;
                }
            }
        }
        return null;
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
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

    private static void parseUrlsFromPageAndStoreIntoDb(Connection connection, Document document) throws SQLException {
        ArrayList<Element> links = document.select("a");

        for (Element aTag : links) {
            String elementLink = aTag.attr("href");
            if (elementLink.startsWith("//")) {
                elementLink = "https:" + elementLink;
            }
            if (!elementLink.toLowerCase().startsWith("javascript")) {
                updateLink2Db(connection, elementLink, "insert into LINK_TO_BE_PROCESSED (link) values (?)");
            }
        }
    }

    private static void updateLink2Db(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static void insertDbIfItsNewsPage(Connection connection, Document document, String link) throws SQLException {
        ArrayList<Element> articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                List<Element> paragraphs = articleTag.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
                // 入库
                try (PreparedStatement statement = connection.prepareStatement("insert into NEWS (title, content, url, created_at, modified_at) values (?, ?, ?, now(), now())")) {
                    statement.setString(1, title);
                    statement.setString(2, content);
                    statement.setString(3, link);
                    statement.executeUpdate();
                }
            }


        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return Jsoup.parse(EntityUtils.toString(entity));
        }
    }

    private static boolean isLoginPage(String link) {
        return link.contains("passport.sina.cn");
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isInterestedLink(String link) {
        return (isIndexPage(link) || isNewsPage(link)) && !isLoginPage(link);
    }

}
