package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PayController {
    @Autowired
    OrderFeignClient orderFeignClient;

    @RequestMapping("pay.html")
    public String pay(@RequestParam("orderId") Long orderId, Model model) {
        OrderInfo orderInfo = orderFeignClient.getOrderById(orderId + "");
        model.addAttribute("orderInfo", orderInfo);
        return "payment/pay";
    }

    /**\
     *支付成功回调跳转
     * @return
     */
    @RequestMapping("success")
    public String success() {
        return "payment/success";
    }
}
