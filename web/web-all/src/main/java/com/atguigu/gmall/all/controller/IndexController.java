package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class IndexController {
    @Autowired
    ProductFeignClient productFeignClient;
    //首页
    @RequestMapping({"/", "index.html"})
    public String getIndexCato(Model model) {
        Result rs = productFeignClient.getBaseCatogory();
        model.addAttribute("list", rs.getData());
        return "index/index.html";
    }
}
