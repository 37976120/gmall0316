package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;

import java.util.List;

public interface BaseAttrInfoService {
    List<BaseAttrInfo> getAttrInfoList(String categoryId1, String categoryId2, String categoryId3);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(String attrId);

    List<SearchAttr> getSearchAttrList(String skuId);
}
