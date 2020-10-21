package com.atguigu.gmall.item.service.Impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeign;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    ListFeign listFeign;

    /**
     * @param skuid
     * @return 1）分类数据
     * 2）sku数据(带图片)
     * 3）价格数据
     * 4）销售属性数据
     */
    @Override

    public Map<String, Object> getItem(String skuid) {

        return getItemInfoAsync(skuid);//使用异步编排+线程池
//        return getItemInfoNoAsync(skuid);//使用串行
    }

    private HashMap<String, Object> getItemInfoAsync(String skuid) {
        long s = System.currentTimeMillis();
        HashMap<String, Object> mapInfo = new HashMap<>();
        CompletableFuture<Void> price = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                //从Product取得数据-->price
                BigDecimal price = productFeignClient.getPrice(skuid);
                mapInfo.put("price", price);
            }
        }, threadPoolExecutor);
        CompletableFuture<SkuInfo> skuInfo = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                //从Product取得数据-->itemInfo
                SkuInfo itemInfo = productFeignClient.getSkuInfo(skuid);
                mapInfo.put("skuInfo", itemInfo);
                return itemInfo;
            }
        }, threadPoolExecutor);
        CompletableFuture<Void> categoryView = skuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo itemInfo) {
                //从Product取得数据-->categoryView
                BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(itemInfo.getCategory3Id().toString());//todo skuId
                mapInfo.put("categoryView", baseCategoryView);
            }
        }, threadPoolExecutor);
        CompletableFuture<Void> spuSaleAttrList = skuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo itemInfo) {
                //从Product取得数据-->销售属性
                List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrs(itemInfo.getSpuId(), Long.parseLong(skuid));
                mapInfo.put("spuSaleAttrList", spuSaleAttrList);
            }
        }, threadPoolExecutor);
        CompletableFuture<Void> valuesSkuJson = skuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo itemInfo) {
                //从Product取得数据-->销售属性对应hash表
                Map<String, String> valuesSkuJson = productFeignClient.getSkuValueIdsMap(itemInfo.getSpuId());
                mapInfo.put("valuesSkuJson", JSON.toJSONString(valuesSkuJson));
            }
        }, threadPoolExecutor);
        //异步热度值更新
        CompletableFuture<Void> hotScore = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                listFeign.incHot(skuid);
            }
        }, threadPoolExecutor);
        CompletableFuture.allOf(price, skuInfo, categoryView, spuSaleAttrList, valuesSkuJson, hotScore).join();
        System.out.println("异步编排查询耗时" + (System.currentTimeMillis() - s));
        return mapInfo;
    }

    private HashMap<String, Object> getItemInfoNoAsync(String skuid) {
        long s = System.currentTimeMillis();
        HashMap<String, Object> mapInfo = new HashMap<>();
        //从Product取得数据-->categoryView
        SkuInfo itemInfo = productFeignClient.getSkuInfo(skuid);
        mapInfo.put("skuInfo", itemInfo);

        //从Product取得数据-->categoryView
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(itemInfo.getCategory3Id().toString());//todo skuId
        mapInfo.put("categoryView", baseCategoryView);

        //从Product取得数据-->price
        BigDecimal price = productFeignClient.getPrice(skuid);
        mapInfo.put("price", price);

        //从Product取得数据-->销售属性
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrs(itemInfo.getSpuId(), Long.parseLong(skuid));
        mapInfo.put("spuSaleAttrList", spuSaleAttrList);

        //从Product取得数据-->销售属性对应hash表
        Map<String, String> valuesSkuJson = productFeignClient.getSkuValueIdsMap(itemInfo.getSpuId());
        mapInfo.put("valuesSkuJson", JSON.toJSONString(valuesSkuJson));
        System.out.println("串行查询耗时" + (System.currentTimeMillis() - s));
        return mapInfo;
    }
}
