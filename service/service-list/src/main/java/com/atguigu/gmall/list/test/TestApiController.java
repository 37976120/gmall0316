package com.atguigu.gmall.list.test;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestApiController {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @RequestMapping("test")
    public String test() {
        //准备查询语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //query
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must();
        boolQueryBuilder.filter();
        searchSourceBuilder.query();
        return null;
    }
}
