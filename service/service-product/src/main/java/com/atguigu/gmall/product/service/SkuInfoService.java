package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.Map;

public interface SkuInfoService {
    IPage<SkuInfo> list(Page<SkuInfo> skuInfoPage);

    void saveSkuInfo(SkuInfo skuInfo);

    SkuInfo getSkuInfo(String skuId);

    BigDecimal getPrice(String skuid);

    Map<String, String> getSkuValueIdsMap(Long spuId);

    void onSale(Long skuid);

    void cancelSale(Long skuid);
}
