package com.atguigu.gmall.list.service;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.util.Map;

public interface ListService {
    void onSale(Long skuId);

    void cancelSale(Long skuId);

    void incHot(String skuid);

    SearchResponseVo list(SearchParam searchParam);
}
