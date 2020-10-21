package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

public interface SpuInfoService {
    IPage<SpuInfo> getSpuList(Page<SpuInfo> pageContain, String category3Id);

    List<BaseTrademark> getTrademarkList();

    List<BaseSaleAttr> baseSaleAttrList();

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> spuImageList(String spuId);

    List<SpuSaleAttr> spuSaleAttrList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrs(Long spuId, long skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId, long skuId);

}
