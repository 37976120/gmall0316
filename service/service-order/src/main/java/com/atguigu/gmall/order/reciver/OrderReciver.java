package com.atguigu.gmall.order.reciver;

import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
public class OrderReciver {
    @Autowired
    OrderService orderService;

    /**
     * 修改订单为已修改
     *
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY, autoDelete = "false"),
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY, autoDelete = "false", durable = "true"),
            key = {MqConst.ROUTING_PAYMENT_PAY}))
    public void orderLock(Message message, Channel channel, String out_trade_no) throws IOException {
        try {
            orderService.updateOrderStatus(out_trade_no);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            if (message.getMessageProperties().isRedelivered()) {
                //todo 记录消费失败日志
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }

    }

}
