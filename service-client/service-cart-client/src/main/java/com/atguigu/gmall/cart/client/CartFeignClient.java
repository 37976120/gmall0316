package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "service-cart")
public interface CartFeignClient {
    @RequestMapping("api/cart/addCart")
    void addCart(@RequestBody CartInfo cartInfo);

    @RequestMapping("api/cart/inner/getCheckedCartList/{userId}")
    List<CartInfo> getCheckedCartList(@PathVariable("userId") String userId);
}
