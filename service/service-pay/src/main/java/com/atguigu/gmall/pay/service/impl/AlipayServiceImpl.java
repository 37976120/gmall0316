package com.atguigu.gmall.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.pay.config.AlipayConfig;
import com.atguigu.gmall.pay.mapper.PaymentInfoMapper;
import com.atguigu.gmall.pay.service.AlipayService;
import com.atguigu.gmall.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    AlipayClient alipayClient;
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    RabbitService rabbitService;

    /**
     * 阿里支付接口对接
     * @param orderInfo
     * @return
     */
    @Override
    public String tradePagePay(OrderInfo orderInfo) {
        String form = "";
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        request.setReturnUrl(AlipayConfig.return_payment_url);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", 0.01);
        StringBuffer subject = new StringBuffer();
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            subject.append(orderDetail.getSkuName() + ",");
        }
        map.put("subject", subject);
        request.setBizContent(JSON.toJSONString(map));
        try {
            form = alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }

    /**
     * 点击提交订单：保存订单并发延时队列
     * @param paymentInfo
     */
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        //延迟队列向阿里查询订单信息
        String exchange = "exchange.delay";
        String routing = "routing.delay";
        paymentInfoMapper.insert(paymentInfo);
        rabbitService.sendDelayMessage(exchange, routing, paymentInfo.getOutTradeNo(), 10);
    }

    /**
     * 用于幂等性检查
     * @param paymentInfo
     *
     */
    @Override
    public String checkSuccess(PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", paymentInfo.getOutTradeNo());
        PaymentInfo paymentInfoFromDb = paymentInfoMapper.selectOne(queryWrapper);
        return paymentInfoFromDb.getPaymentStatus();
    }

    /**
     * 支付成功回调更新订单状态
     * @param paymentInfo
     */
    @Override
    public void update(PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", paymentInfo.getOutTradeNo());
        paymentInfoMapper.update(paymentInfo, queryWrapper);
        //发送支付成功消息
        rabbitService.convertAndSend(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY, MqConst.ROUTING_PAYMENT_PAY, paymentInfo.getOutTradeNo());
    }
}
