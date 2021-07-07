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

public class Main {
    private static final String H2_CONNECTION = "jdbc:h2:file:C:\\WorkSpace\\HCSP\\Thread\\crawler\\news";
    private static final String H2_USERNAME = "root";
    private static final String H2_PASSWORD = "root";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        final Connection connection = DriverManager.getConnection(H2_CONNECTION, H2_USERNAME, H2_PASSWORD);

        while (true) {
            List<String> originLinkPool = loadUrlsFromDb(connection, "select link from LINK_TO_BE_PROCESSED");
            if (originLinkPool.isEmpty()) {
                break;
            }
            // 删除处理过的链接(内存、数据库中都删除)
            String link = originLinkPool.remove(originLinkPool.size() - 1);
            updateLink2Db(connection, link, "delete from LINK_TO_BE_PROCESSED WHERE LINK = ?");

            if (link.startsWith("//")) {
                link = "https:" + link;
            }
            System.out.println(link);
            // 已经处理过的不进行处理
            if (isLinkProcessed(connection, link)) {
                continue;
            }
            Document document = httpGetAndParseHtml(link);
            ArrayList<Element> links = document.select("a");
            for (Element aTag : links) {
                String elementLink = aTag.attr("href");
                if (isNewsPage(elementLink) && !isLoginPage(elementLink)) {
                    updateLink2Db(connection, elementLink, "insert into LINK_TO_BE_PROCESSED (link) values (?)");
                }
            }
            insertDbIfItsNewsPage(document);
            updateLink2Db(connection, link, "insert into LINK_ALREADY_PROCESSED (link) values (?)");
        }

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

    private static List<String> loadUrlsFromDb(Connection connection, String sql) throws SQLException {
        List<String> links = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                links.add(resultSet.getString(1));
            }
        }
        return links;
    }

    private static void updateLink2Db(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static void insertDbIfItsNewsPage(Document document) {
        ArrayList<Element> articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            String title = articleTags.get(0).child(0).text();
            System.out.println(title);
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
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


}
