package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.list.client.ListFeign;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class SkuInfoApiController {

    @Autowired
    SkuInfoService skuInfoService;

    @RequestMapping("list/{pageNum}/{pageSize}")
    public Result list(@PathVariable("pageNum") Long pageNum, @PathVariable("pageSize") Long pageSize) {
        Page<SkuInfo> skuInfoPage = new Page<SkuInfo>(pageNum, pageSize);
        IPage<SkuInfo> data = skuInfoService.list(skuInfoPage);
        return Result.ok(data);
    }

    @RequestMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody() SkuInfo skuInfo) {
        System.out.println("!!");
        skuInfoService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    @RequestMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuid) {
        skuInfoService.onSale(skuid);
        return Result.ok();
    }

    @RequestMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuid) {
        skuInfoService.cancelSale(skuid);
        return Result.ok();
    }
}
