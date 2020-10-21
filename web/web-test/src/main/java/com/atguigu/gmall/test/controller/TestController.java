package com.atguigu.gmall.test.controller;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class TestController {
    @Value("${myname}")
    String myname;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RedisTemplate redisTemplate;


    @RequestMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request) {
        Integer ticket = (Integer) redisTemplate.opsForValue().get("ticket");
        ticket--;
        redisTemplate.opsForValue().set("ticket", ticket);
        String s = "getRemotePort()" + request.getRemotePort() + "\n" + ticket;
        System.out.println(s);
        return s;
    }

    @RequestMapping("testLock")
    @ResponseBody
    public String testLock(HttpServletRequest request) {
        RLock lock = redissonClient.getLock("lock");
        String s;
        try {
            lock.lock();
            Integer ticket = (Integer) redisTemplate.opsForValue().get("ticket");
            ticket--;
            redisTemplate.opsForValue().set("ticket", ticket);
            s = "getRemotePort()" + request.getRemotePort() + "\n" + ticket;
            System.out.println(s);
        } finally {
            lock.unlock();
        }
        return s;
    }
}
