package com.atguigu.gmall.product.client;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(value = "service-product")
public interface ProductFeignClient {

    @RequestMapping("api/product/inner/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable("category3Id") String category3Id);

    @RequestMapping("api/product/inner/getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable("skuId") String skuid);

    @RequestMapping("api/product/inner/getPrice/{skuId}")
    BigDecimal getPrice(@PathVariable("skuId") String skuid);

    @RequestMapping("api/product/inner/getMySpuSaleAttrs/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrs(@PathVariable("spuId") Long spuId, @PathVariable("skuId") long skuId);

    @RequestMapping("api/product/inner/getSkuValueIdsMap/{spuId}")
    Map<String, String> getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    @RequestMapping("api/product/getBaseCategoryList")
    Result getBaseCatogory();

    @RequestMapping("api/product/inner/getTrademark/{tmId}")
    BaseTrademark getTrademark(@PathVariable("tmId") Long tmId);


    @RequestMapping("api/product/inner/getAttrList/{skuId}")
    List<SearchAttr> getSearchAttrList(@PathVariable("skuId") String skuId);

}
