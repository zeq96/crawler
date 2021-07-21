package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.List;

public class SmokeTest {
    @Test
    public void smoke() {

    }

    @Test
    public void getInstantTest() {
        String resource = "db/mybatis/config.xml";
        SqlSessionFactory sqlSessionFactory;
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            List<News> currentNews = sqlSession.selectList("com.github.hcsp.MockMapper.selectTheFirst500News");
            currentNews.forEach(news -> System.out.println(news.getCreatedAt().atZone(ZoneId.of("GMT+8")) + "," + news.getModifiedAt()));
        }

    }


}
