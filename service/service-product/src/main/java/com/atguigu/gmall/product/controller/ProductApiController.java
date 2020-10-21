package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/product")
public class ProductApiController {
    @Autowired
    BaseCategoryService baseCategoryService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SpuInfoService spuInfoService;

    @Autowired
    BaseTrademarkService baseTrademarkService;

    @Autowired
    BaseAttrInfoService baseAttrInfoService;

    @RequestMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") String category3Id) {
        return baseCategoryService.getCategoryView(category3Id);
    }

    @RequestMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") String skuId) {
        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuId);
        return skuInfo;
    }

    @RequestMapping("inner/getPrice/{skuId}")
    BigDecimal getPrice(@PathVariable("skuId") String skuid) {
        BigDecimal price = skuInfoService.getPrice(skuid);
        return price;
    }

    @RequestMapping("inner/getMySpuSaleAttrs/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrs(@PathVariable("spuId") Long spuId, @PathVariable("skuId") long skuId) {
        List<SpuSaleAttr> data = spuInfoService.getSpuSaleAttrListCheckBySku(spuId, skuId);//.getSpuSaleAttrs(spuId, skuId);
        return data;
    }

    @RequestMapping("inner/getSkuValueIdsMap/{spuId}")
    Map<String, String> getSkuValueIdsMap(@PathVariable("spuId") Long spuId) {
        Map<String, String> data = skuInfoService.getSkuValueIdsMap(spuId);
        return data;
    }
    //首页的分类树
    @RequestMapping("getBaseCategoryList")
    Result getBaseCatogory() {
        List<JSONObject> data = baseCategoryService.getBaseCatogory();
        return Result.ok(data);
    }

    @RequestMapping("inner/getTrademark/{tmId}")
    BaseTrademark getTrademark(@PathVariable("tmId") Long tmId) {
        return baseTrademarkService.getTrademark(tmId);
    }


    @RequestMapping("inner/getAttrList/{skuId}")
    List<SearchAttr> getSearchAttrList(@PathVariable("skuId") String skuId) {
        List<SearchAttr> data = baseAttrInfoService.getSearchAttrList(skuId);
        return data;
    }
}
