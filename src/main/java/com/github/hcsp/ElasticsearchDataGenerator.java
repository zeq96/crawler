package com.github.hcsp;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchDataGenerator {
    public static void main(String[] args) {
        String resource = "db/mybatis/config.xml";
        SqlSessionFactory sqlSessionFactory;
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<News> currentNews = getNewsFromMysql(sqlSessionFactory);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> generateData(currentNews)).start();
        }
    }

    private static void generateData(List<News> currentNews) {
        try (RestHighLevelClient client =
                     new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            for (int i = 0; i < 200; i++) {
                IndexRequest request = new IndexRequest("news");
                BulkRequest bulkRequest = new BulkRequest();
                for (News news : currentNews) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", news.getTitle());
                    data.put("content", news.getContent());
                    data.put("url", news.getUrl());
                    data.put("createdAt", news.getCreatedAt());
                    data.put("modifiedAt", news.getModifiedAt());
                    bulkRequest.add(request.source(data, XContentType.JSON));
                }
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println("Current Thread:" + Thread.currentThread().getName() +
                        ", Finishes" + i + ":" + bulkResponse.status().getStatus());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从mysql news中查询前500条数据
     *
     * @param sqlSessionFactory mybatis sqlSessionFactory
     * @return 查询出的的News集合
     */
    private static List<News> getNewsFromMysql(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return sqlSession.selectList("com.github.hcsp.MockMapper.selectTheFirst500News");
        }
    }
}
