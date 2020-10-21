package com.atguigu.gmall.list.client;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//接口
@FeignClient(value = "service-list")
public interface ListFeign {
    @RequestMapping("api/list/onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/list/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/list/incHot/{skuId}")
    void incHot(@PathVariable("skuId") String skuid);

    @PostMapping("api/list/list")
    Result list(@RequestBody SearchParam searchParam);
}
