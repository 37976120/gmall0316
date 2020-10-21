package com.atguigu.gmall.list.test;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public class EsApitest {

    public static void main(String[] args) {
        //es的常用的API
        ElasticsearchTemplate elasticsearchTemplate = null;
        ElasticsearchRepository elasticsearchRepository = null;
        //高阶REST风格API 做ES的复合查询
        RestHighLevelClient restHighLevelClient = null;

    }
}
