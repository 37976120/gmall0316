package com.atguigu.gmall.pay.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

public interface AlipayService {
    String tradePagePay(OrderInfo orderInfo);

    void savePaymentInfo(PaymentInfo paymentInfo);

    String checkSuccess(PaymentInfo paymentInfo);

    void update(PaymentInfo paymentInfo);
}
