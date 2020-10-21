package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class ItemApiController {
    @Autowired
    ItemService itemService;

    @RequestMapping("api/item/{skuid}")
    Result getItem(@PathVariable("skuid") String skuid) {
        Map<String, Object> mapInfo = itemService.getItem(skuid);
        return Result.ok(mapInfo);
    }

}
