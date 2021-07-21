package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        String resource = "db/mybatis/config.xml";
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized String getNextLinkThenDelete() throws SQLException {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            String nextLink = sqlSession.selectOne("com.github.hcsp.MyMapper.selectNextAvalilableLink");
            if (nextLink != null) {
                sqlSession.delete("com.github.hcsp.MyMapper.deleteLink", nextLink);
            }
            return nextLink;
        }
    }

    @Override
    public void insertNews2Db(String title, String content, String link) throws SQLException {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            sqlSession.insert("com.github.hcsp.MyMapper.insertNews", new News(title, content, link));
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            int count = sqlSession.selectOne("com.github.hcsp.MyMapper.selectCountLink", link);
            return count != 0;
        }
    }

    @Override
    public void insertProcessedLink(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "LINK_ALREADY_PROCESSED");
        param.put("link", link);
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            sqlSession.insert("com.github.hcsp.MyMapper.insertLink", param);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "LINK_TO_BE_PROCESSED");
        param.put("link", link);
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            sqlSession.insert("com.github.hcsp.MyMapper.insertLink", param);
        }
    }

}
