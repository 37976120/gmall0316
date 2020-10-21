package com.atguigu.gmall.item.client;

import com.atguigu.gmall.commom.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "service-item")
public interface ItemFeignClient {

    @RequestMapping("api/item/{skuid}")
    Result getItem(@PathVariable("skuid") String skuid);

}
