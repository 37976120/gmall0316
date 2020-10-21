package com.atguigu.gmall.seckill.client;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.activity.OrderRecode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "service-activity")
public interface SeckillFeignClient {
    @RequestMapping("/api/activity/seckill/findAll")
    Result findAll();

    @RequestMapping("/api/activity/seckill/getItem/{skuId}")
    Result getItem(@PathVariable("skuId") String skuId);

    @RequestMapping("/api/activity/seckill/auth/getPreOrder/{userId}")
    OrderRecode getPreOrder(@PathVariable("userId") String userId);

}
