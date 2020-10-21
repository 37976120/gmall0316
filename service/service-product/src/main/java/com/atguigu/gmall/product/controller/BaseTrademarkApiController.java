package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class BaseTrademarkApiController {
    @Autowired
    BaseTrademarkService baseTrademarkService;

    @RequestMapping("baseTrademark/{page}/{limit}")
    public Result baseTrademark(@PathVariable("page") Long page, @PathVariable("limit") Long limit) {
        Page<BaseTrademark> trademarkPage = new Page<>(page, limit);
        IPage<BaseTrademark> data = baseTrademarkService.baseTrademark(trademarkPage);
        Result<IPage<BaseTrademark>> ok = Result.ok(data);
        return ok;
    }
}
