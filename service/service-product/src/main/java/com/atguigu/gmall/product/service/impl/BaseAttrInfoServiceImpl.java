package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.product.mapper.BaseAttrValueMaaper;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService {
    @Autowired
    BaseAttrInfoMapper baseattrInfoMapper;

    @Autowired
    BaseAttrValueMaaper baseAttrValueMaaper;

    @Override
    public List<BaseAttrInfo> getAttrInfoList(String categoryId1, String categoryId2, String categoryId3) {
        QueryWrapper<BaseAttrInfo> wrapperInfo = new QueryWrapper<>();
        wrapperInfo.eq("category_level", 3);
        wrapperInfo.eq("category_id", categoryId3);
        List<BaseAttrInfo> baseAttrInfos = baseattrInfoMapper.selectList(wrapperInfo);
        for (BaseAttrInfo baseAttrInfo : baseAttrInfos) {
            Long attrInfoId = baseAttrInfo.getId();
            QueryWrapper<BaseAttrValue> wrapperAttr = new QueryWrapper<>();
            wrapperAttr.eq("attr_id", attrInfoId);
            List<BaseAttrValue> baseAttrValues = baseAttrValueMaaper.selectList(wrapperAttr);
            baseAttrInfo.setAttrValueList(baseAttrValues);
        }
        return baseAttrInfos;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null) {
            baseattrInfoMapper.updateById(baseAttrInfo);
            //有ID为修改(先删除子类所有)
            QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
            wrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMaaper.delete(wrapper);
        } else {
            //没有ID就添加
            baseattrInfoMapper.insert(baseAttrInfo);
        }
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMaaper.insert(baseAttrValue);
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id", attrId);
        return baseAttrValueMaaper.selectList(wrapper);
    }

    @Override
    public List<SearchAttr> getSearchAttrList(String skuId) {
        List<SearchAttr> data = baseattrInfoMapper.getSearchAttrList(skuId);
        return data;
    }
}
