package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * 将爬到的新闻数据，Mock到10000条
 */
public class MockData {

    public static void main(String[] args) {
        String resource = "db/mybatis/config.xml";
        SqlSessionFactory sqlSessionFactory;
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mockData(sqlSessionFactory, 100_0000);

    }

    @SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
    private static void mockData(SqlSessionFactory sqlSessionFactory, int howMany) {
        Random random = new Random();
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> newsList = sqlSession.selectList("com.github.hcsp.MockMapper.selectNews");
            int count = howMany - newsList.size();
            try {
                while (count-- > 0) {
                    int index = random.nextInt(newsList.size());
                    // 浅拷贝构造函数，不影响list中的原始数据
                    News newsTobeInsert = new News(newsList.get(index));

                    Instant currentTime = newsTobeInsert.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(60 * 60 * 24 * 365));
                    newsTobeInsert.setCreatedAt(currentTime);
                    newsTobeInsert.setModifiedAt(currentTime);

                    sqlSession.insert("com.github.hcsp.MockMapper.insertNews", newsTobeInsert);
                    System.out.println("Left: " + count);
                    if (count % 2000 == 0) {
                        sqlSession.flushStatements();
                    }
                }
                sqlSession.commit();
                System.out.println("模拟100万条数据完成！！！");
            } catch (Exception e) {
                sqlSession.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
