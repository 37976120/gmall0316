package com.atguigu.gmall.order.client;

import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "service-order")
public interface OrderFeignClient {

    @RequestMapping("api/order/inner/genTradeNo/{userId}")
    public String genTradeNo(@PathVariable("userId") String userId);

    @RequestMapping("api/order/inner/getOrderById/{orderId}")
    OrderInfo getOrderById(@PathVariable("orderId") String orderId);

    @RequestMapping("api/order/inner/saveSeckillOrder")
    OrderInfo saveSeckillOrder(@RequestBody OrderInfo orderInfo);
}
