package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.management.relation.RelationSupport;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ItemController {

    @Autowired
    //单独封装的一个框架包，用maven解耦合，不提出来有些耦合，
    ItemFeignClient itemFeignClient;

    @RequestMapping("{skuid}.html")
    public String index(@PathVariable("skuid") Long skuid, Model model) {
        Result item = itemFeignClient.getItem(skuid.toString());
        Map<String, Object> data = (Map<String, Object>) item.getData();
        model.addAllAttributes(data);
        return "item/index";
    }

}
