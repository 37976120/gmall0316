package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SpuInfoServiceImpl implements SpuInfoService {
    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseTrademarkMaaper baseTrademarkMaaper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Override
    public IPage<SpuInfo> getSpuList(Page<SpuInfo> pageContain, String category3Id) {
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id", category3Id);
        return spuInfoMapper.selectPage(pageContain, wrapper);
    }

    @Override
    public List<BaseTrademark> getTrademarkList() {
        return baseTrademarkMaaper.selectList(null);
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //todo 空指针判断  批处理
        spuInfoMapper.insert(spuInfo);
        Long spuInfoId = spuInfo.getId();
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //保存spuSaleAttr和保存spuSaleAttrValue
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfoId);
            spuSaleAttrMapper.insert(spuSaleAttr);
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfoId);
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }
        }
        //保存图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfoId);
            spuImageMapper.insert(spuImage);
        }
    }

    @Override
    public List<SpuImage> spuImageList(String spuId) {
        QueryWrapper<SpuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(wrapper);
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        QueryWrapper<SpuSaleAttr> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectList(wrapper);
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {
            Long id = spuSaleAttr.getId();
            QueryWrapper<SpuSaleAttrValue> wrapperValue = new QueryWrapper<>();
            wrapperValue.eq("base_sale_attr_id", id);
            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(wrapperValue);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
        }
        return spuSaleAttrs;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrs(Long spuId, long skuId) {
        QueryWrapper<SpuSaleAttr> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectList(wrapper);
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {
            QueryWrapper<SpuSaleAttrValue> valueQueryWrapper = new QueryWrapper<>();
            valueQueryWrapper.eq("base_sale_attr_id", spuSaleAttr.getBaseSaleAttrId());
            valueQueryWrapper.eq("spu_id", spuId);
            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(valueQueryWrapper);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
        }
        return spuSaleAttrs;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId, long skuId) {
        List<SpuSaleAttr> data = spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(spuId, skuId);
        return data;
    }

}
