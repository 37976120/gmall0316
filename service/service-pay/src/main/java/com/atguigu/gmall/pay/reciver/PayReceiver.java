package com.atguigu.gmall.pay.reciver;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Configuration
public class PayReceiver {

    @RabbitListener(queues = "queue.delay.1")
    public void paySuccess(String outTradeNo, Message message, Channel channel) throws IOException {
        //todo 用阿里接口查订单状态
/*
        // 调用AlipayService查询延迟结果
        PaymentInfo paymentInfo = alipayService.checkPayStatus(outTradeNo);
        // 如果已经支付修改，则修改支付服务，发送支付成功队列
        // 修改支付状态
        // 进行幂等性检查
        String success = alipayService.checkPaySuccess(paymentInfo);
        if(!success.equals("PAID")){
            alipayService.update(paymentInfo);
        }
        // 如果没有支付，继续发送延迟队列
*/
        String msg = new String(message.getBody());
        System.out.println("接口查询订单状态");
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
