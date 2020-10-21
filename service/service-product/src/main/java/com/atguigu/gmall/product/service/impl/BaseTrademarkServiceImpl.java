package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMaaper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseTrademarkServiceImpl implements BaseTrademarkService {
    @Autowired
    BaseTrademarkMaaper baseTrademarkMaaper;

    @Override
    public IPage<BaseTrademark> baseTrademark(Page<BaseTrademark> trademarkPage) {
        IPage<BaseTrademark> iPage = baseTrademarkMaaper.selectPage(trademarkPage, null);
        return iPage;
    }

    @Override
    public BaseTrademark getTrademark(Long tmId) {
        return baseTrademarkMaaper.selectById(tmId);
    }
}
