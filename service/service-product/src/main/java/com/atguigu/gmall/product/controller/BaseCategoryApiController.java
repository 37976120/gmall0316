package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.BaseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class BaseCategoryApiController {
    @Autowired
    BaseCategoryService baseCategoryService;

    @RequestMapping("getCategory1")
    public Result getCategory1() {
        List<BaseCategory1> data = baseCategoryService.getCategory1();
        return Result.ok(data);
    }

    @RequestMapping("getCategory2/{Category1Id}")
    public Result getCategory2(@PathVariable("Category1Id") String Category1Id) {
        List<BaseCategory2> data = baseCategoryService.getCategory2(Category1Id);
        return Result.ok(data);
    }

    @RequestMapping("getCategory3/{Category2Id}")
    public Result getCategory3(@PathVariable("Category2Id") String Category2Id) {
        List<BaseCategory3> data = baseCategoryService.getCategory3(Category2Id);
        return Result.ok(data);
    }

}
