package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.service.RabbitService;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Soundbank;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/user/passport")
public class UserApiController {
    @Autowired
    UserService userService;

    @Autowired
    RabbitService rabbitService;

    @RequestMapping("a")
    public String a() {
        return "a";
    }

    @RequestMapping("inner/check/{check}")
    public String check(@PathVariable("check") String check) {
        return userService.verify(check);
    }

    @RequestMapping("login")
    public Result login(@RequestBody UserInfo userInfo) {
        Map<String, Object> map = userService.login(userInfo);
        UserInfo userInfo1 = (UserInfo) map.get("userInfo");
        map.put("loginName", userInfo1.getLoginName());
        map.put("nickName", userInfo1.getNickName());
//        rabbitService.send(); todo
        return Result.ok(map);
    }

    @RequestMapping("inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable("userId") String userId) {
        List<UserAddress> addressList = userService.findUserAddressListByUserId(userId);
        return addressList;
    }
}
