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
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        List<String> originLinkPool = new ArrayList<>();
        List<String> processedLinkPool = new ArrayList<>();
        originLinkPool.add("https://sina.cn");

        while (true) {
            if (originLinkPool.isEmpty()) {
                break;
            }
            String link = originLinkPool.remove(originLinkPool.size() - 1);
            if (link.startsWith("//")) {
                link = "https:" + link;
            }
            System.out.println(link);
            // 已经处理过的不进行处理
            if (processedLinkPool.contains(link)) {
                continue;
            }
            Document document = httpGetAndParseHtml(link);
            ArrayList<Element> links = document.select("a");
            links.stream().map(aTag -> aTag.attr("href")).filter(elementLink -> isNewsPage(elementLink) && !isLoginPage(elementLink)).forEach(originLinkPool::add);

            insertDbIfItsNewsPage(document);
            processedLinkPool.add(link);
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
