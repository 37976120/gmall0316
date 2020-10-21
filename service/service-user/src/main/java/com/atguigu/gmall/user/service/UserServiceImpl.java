package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public String verify(String token) {
        String tokened = (String) redisTemplate.opsForValue().get("user:login:" + token);
        return tokened;
    }

    @Override
    public Map<String, Object> login(UserInfo userInfo) {
        Map<String, Object> map = new HashMap<>();
        String pwd = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        String loginName = userInfo.getLoginName();
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("login_name", loginName);
        userInfoQueryWrapper.eq("passwd", pwd);
        UserInfo userInfo1 = userMapper.selectOne(userInfoQueryWrapper);
        if (userInfo1 != null) {
            map.put("userInfo", userInfo1);
            String token = UUID.randomUUID().toString().replace("-", "");
            map.put("token", token);
            redisTemplate.opsForValue().set("user:login:" + token, userInfo1.getId().toString());
        }
        return map;
    }

    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        QueryWrapper<UserAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserAddress> addressList = userAddressMapper.selectList(wrapper);
        return addressList;
    }


}
