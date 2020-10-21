package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class SpuInfoApiController {
    @Autowired
    SpuInfoService spuInfoService;

    @RequestMapping("{page}/{limit}")
    public Result getSpuList(@PathVariable("page") Long page,
                             @PathVariable("limit") Long limit,
                             @RequestParam("category3Id") String category3Id) {
        Page<SpuInfo> pageContain = new Page<>(page, limit);
        IPage<SpuInfo> data = spuInfoService.getSpuList(pageContain, category3Id);
        return Result.ok(data);
    }

    @RequestMapping("baseTrademark/getTrademarkList")
    public Result getTrademarkList() {
        List<BaseTrademark> data = spuInfoService.getTrademarkList();
        return Result.ok(data);
    }

    @RequestMapping("baseSaleAttrList")
    public Result baseSaleAttrList() {
        List<BaseSaleAttr> data = spuInfoService.baseSaleAttrList();
        return Result.ok(data);
    }

    @RequestMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody() SpuInfo spuInfo) {
        spuInfoService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    @RequestMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") String spuId) {
        List<SpuImage> data = spuInfoService.spuImageList(spuId);
        return Result.ok(data);
    }

    @RequestMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") String spuId) {
        List<SpuSaleAttr> data = spuInfoService.spuSaleAttrList(spuId);
        return Result.ok(data);
    }


}
