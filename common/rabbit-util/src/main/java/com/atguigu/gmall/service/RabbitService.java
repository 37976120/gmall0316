package com.atguigu.gmall.service;

public interface RabbitService {
    void send(String ex, String rt, Object msg);

    void convertAndSend(String exchangeDirectPaymentPay, String routingPaymentPay, String outTradeNo);

    void sendDelayMessage(String exchange, String routing, String outTradeNo, int i);
}
