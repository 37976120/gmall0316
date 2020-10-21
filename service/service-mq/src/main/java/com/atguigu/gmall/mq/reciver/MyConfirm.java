package com.atguigu.gmall.mq.reciver;


import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Configuration
@Component
public class MyConfirm {

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "queue.confirm", autoDelete = "false"),
                                            exchange = @Exchange(value = "exchange.confirm", autoDelete = "true"),
                                            key = {"routing.confirm"})
    )
    public void process(Message message, Channel channel) {
        String s = new String(message.getBody());
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            System.out.println("消费消息" + s);
            channel.basicAck(tag, false);//false:不批量确认
        } catch (Exception e) {
            if (message.getMessageProperties().getRedelivered()) {
                try {
//                    // 拒绝消息，requeue=false 表示不再重新入队，如果配置了死信队列则进入死信队列
//                    channel.basicReject(tag, false);
                    //放弃
                    channel.basicAck(tag, false);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                try {
                    channel.basicNack(tag, false, true);// 第一个false是是否批量确认，第二个true是是否重新投递
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

    }
}
