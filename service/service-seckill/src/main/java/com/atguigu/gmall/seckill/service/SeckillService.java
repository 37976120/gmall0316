package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillService {
    void putGoods();

    List<SeckillGoods> findAll();

    Result getitem(String skuId);

    void seckillOrder(String skuId, String userId);

    void consumeSeckillOrder(Long skuId, String userId);

    Result checkOrder(String skuId,String userId);
}
