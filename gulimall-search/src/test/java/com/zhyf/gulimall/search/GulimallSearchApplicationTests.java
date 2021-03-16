package com.zhyf.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.zhyf.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSearchApplicationTests {
    @Qualifier("esRestClient")
    @Autowired
    private RestHighLevelClient esRestClient;

    @Test
    void contextLoads() {
        System.out.println(esRestClient);
    }

    @Test
    void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("11");
//		indexRequest.source("userName", "zhayf", "age",11,"gender","f");
        User user = new User();
        user.setAge(11);
        user.setName("jes");
        user.setGender("F");
        String string = JSON.toJSONString(user);
        indexRequest.source(string, XContentType.JSON);
        IndexResponse index = esRestClient.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Test

}

@Data
class User {
    private String name;
    private String gender;
    private Integer age;
}