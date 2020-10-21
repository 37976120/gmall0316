package com.atguigu.gmall.seckill.reciver;

import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.seckill.service.SeckillService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Configuration
public class SeckillOrdeReceiver {
    @Autowired
    SeckillService seckillService;

    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER, durable = "true"),
            key = {MqConst.ROUTING_SECKILL_USER},
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER, durable = "true")
    ))
    void seckillOrder(Message message, Channel channel, UserRecode messageSeckillUserRecode) {
        seckillService.consumeSeckillOrder(messageSeckillUserRecode.getSkuId(), messageSeckillUserRecode.getUserId());
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
