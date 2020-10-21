package com.atguigu.gmall.service.Impl;

import com.atguigu.gmall.service.RabbitService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitServiceImpl implements RabbitService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void send(String ex, String rt, Object msg) {
        rabbitTemplate.convertAndSend(ex, rt, msg);
        System.out.println("发送消息");
    }

    @Override
    public void convertAndSend(String exchangeDirectPaymentPay, String routingPaymentPay, String outTradeNo) {
        rabbitTemplate.convertAndSend(exchangeDirectPaymentPay, routingPaymentPay, outTradeNo);
    }

    @Override
    public void sendDelayMessage(String exchange, String routing, String outTradeNo, int i) {
        rabbitTemplate.convertAndSend(exchange, routing, outTradeNo, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(i * 1000);//默认单位为：毫秒
                return message;
            }
        });
    }
}
