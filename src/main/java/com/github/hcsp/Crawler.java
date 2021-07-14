package com.github.hcsp;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Crawler {
    CrawlerDao dao = new DataAccessObject();

    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }

    public void run() throws SQLException, IOException {
        String link;
        while ((link = dao.getNextLinkThenDelete()) != null) {
            // 已经处理过的不进行处理
            if (dao.isLinkProcessed(link)) {
                continue;
            }
            // 只关心news.sina.cn , 并且过滤登录页面
            if (isInterestedLink(link)) {
                System.out.println(link);
                Document document = httpGetAndParseHtml(link);

                parseUrlsFromPageAndStoreIntoDb(document);

                insertDbIfItsNewsPage(document, link);

                dao.updateLink2Db(link, "insert into LINK_ALREADY_PROCESSED (link) values (?)");
            }
        }
    }

    private void parseUrlsFromPageAndStoreIntoDb(Document document) throws SQLException {
        ArrayList<Element> links = document.select("a");

        for (Element aTag : links) {
            String elementLink = aTag.attr("href");
            if (elementLink.startsWith("//")) {
                elementLink = "https:" + elementLink;
            }
            if (!elementLink.toLowerCase().startsWith("javascript")) {
                dao.updateLink2Db(elementLink, "insert into LINK_TO_BE_PROCESSED (link) values (?)");
            }
        }
    }

    private void insertDbIfItsNewsPage(Document document, String link) throws SQLException {
        ArrayList<Element> articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                List<Element> paragraphs = articleTag.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
                // 入库
                dao.insertNews2Db(title, content, link);
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