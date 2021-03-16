package com.zhyf.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.zhyf.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
    void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引在哪里检索
        searchRequest.indices("bank");
        // 指定检索条件 DSL
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        // 按照年龄的值分布聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        // 平均薪资的聚合
        AvgAggregationBuilder balanceAgg = AggregationBuilders.avg("balanceAvg").field("balance");
        System.out.println(searchSourceBuilder.toString());
        searchSourceBuilder.aggregation(ageAgg);
        searchSourceBuilder.aggregation(balanceAgg);
        searchRequest.source(searchSourceBuilder);
        // 执行检索
        SearchResponse searchResponse = esRestClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        // 拿到响应 分析结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit item : searchHits) {
            System.out.println(item.toString());
        }
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println(keyAsString);
        }
        Avg balanceAvg = aggregations.get("balanceAgg");

    }

}

@Data
class User {
    private String name;
    private String gender;
    private Integer age;
}