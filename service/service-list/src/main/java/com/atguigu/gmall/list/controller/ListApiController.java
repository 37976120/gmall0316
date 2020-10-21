package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/list")
public class ListApiController {
    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    ListService listService;

    @RequestMapping("/createMapping")
    private Result createMapping() {
        // 调用es的api导入数据结构
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    @RequestMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {
        listService.onSale(skuId);
        return Result.ok();
    }

    @RequestMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId) {
        listService.cancelSale(skuId);
        return Result.ok();
    }

    @RequestMapping("incHot/{skuId}")
    void incHot(@PathVariable("skuId") String skuid) {
        listService.incHot(skuid);
    }

    @PostMapping("list")
    Result list(@RequestBody SearchParam searchParam) {
        SearchResponseVo result = listService.list(searchParam);

        return Result.ok(result);
    }
}
