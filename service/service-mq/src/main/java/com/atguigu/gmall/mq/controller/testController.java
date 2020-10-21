package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class testController {

    @Autowired
    RabbitService rabbitService;

    @RequestMapping("send")
    public String send() {
        String ex = "exchange.confirm";
        String rt = "routing.confirm";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rabbitService.send(ex, rt, sdf.format(new Date()));
        return "";
    }
}
