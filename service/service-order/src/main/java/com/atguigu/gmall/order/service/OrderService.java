package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

public interface OrderService {
    String genTradeNo(String userId);

    OrderInfo save(OrderInfo orderInfo);

    boolean checkTradeNo(String tradeNo,String userid);

    OrderInfo getOrderById(String orderId);

    void updateOrderStatus(String out_trade_no);
}
