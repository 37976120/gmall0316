package com.atguigu.gmall.user.client;

import com.atguigu.gmall.model.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "service-user")
public interface UserFeignClient {
    @RequestMapping("api/user/passport/a")
    public String a();

    @RequestMapping("api/user/passport/inner/check/{check}")
    public String check(@PathVariable("check") String check);

    @RequestMapping("api/user/passport/inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable("userId") String userId);
}