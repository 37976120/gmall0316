package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@Controller
public class TestController {

    @RequestMapping("test")
    public String test(ModelMap modelMap) {
        ArrayList<String> integers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            integers.add("元素" + i);
        }
        modelMap.addAttribute("value", "hello world");
        modelMap.addAttribute("list", integers);
        modelMap.addAttribute("gname", "<span style=\"color:green\">宝强</span>");
        return "test";
    }

}
