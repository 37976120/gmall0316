package com.atguigu.gmall.pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.pay.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping("api/payment")
public class PayApiController {
    //自己封装的阿里Client
    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    AlipayService alipayService;

    //支付成功回调接口：更新系统订单支付状态为已支付，发送支付成功消息
    @RequestMapping("alipay/callback/return")
    public String callback(HttpServletRequest request) {
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_no = request.getParameter("trade_no");
        String queryString = request.getQueryString();
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentInfo.setTradeNo(trade_no);
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.toString());
        paymentInfo.setCallbackContent(queryString);

        //保证幂等性,先查询订单的状态，是已修改就直接返回成功
        if (!alipayService.checkSuccess(paymentInfo).equals(PaymentStatus.PAID.toString())) {
            alipayService.update(paymentInfo);
        }

        String paySuccess = "<form name='punchout_form' method='get' action='http://payment.gmall.com/success'>\n" +
                "</form>\n" +
                "<script>document.forms[0].submit();</script>";
        return paySuccess;

    }

    @RequestMapping("alipay/submit/{orderId}")
    public String alipay(@PathVariable("orderId") String orderId) {
        //提交支付任务给阿里返回支付页面的表单
        OrderInfo orderInfo = orderFeignClient.getOrderById(orderId);
        String from = alipayService.tradePagePay(orderInfo);

        //保存支付信息到数据库
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(Long.parseLong(orderId));
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setPaymentType(PaymentType.ALIPAY.getComment());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        // paymentInfo.setCallbackTime();
        // paymentInfo.setTradeNo();
        // paymentInfo.setCallbackContent();
        //TODO  幂等
        alipayService.savePaymentInfo(paymentInfo);
        return from;
    }

    private String aliPayDemo() {
        String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC8Z7EZmanxyFGsK4LrIUeKKrrGxWAHIgPmUV8TtZDs+jeplJSw1ckSY63QhEU444D5qd6xruJHBuB33HG+ik4n8N8nRWi3AtMgpC061oq2DcgtIKMmQHO7/poYDwbpDZrOWXIyiNshFfUOSTUpnrS8UvEks6n6xR/G72r2FG07oZzO7g3XsPMr73wpYajMYC/bhTm6CJGEWZikONNDFkQpVHa+zgitwsqlBuvBvVwGwOHA9B8aRfokwAMl6BDXKoH8BNnSEMpWSTRSwbssayXAQWNU7XKDKGozbn4U2dEbl8GCFzikI/T7ybTNm5gs46ZZBGlq/YB4+v4D3t74Vl6nAgMBAAECggEAOidzhehliYkAlLk1huhV0bMQxewEkQ8RzxTM2SORIWS2q7R+FPtYPkHgU92QFFg85lNltsi5dZ0MylKUFXFRYIi8CL4m7V6E1q12fJPeawVkBXHuig8Y6i1TWRvCUUtuvkTjt++AW/0QECHOtBMVzI95eY+vZwVToq8h/+UcNmxKyVt66Qpo4+r+cUvlvGX5mXgQVC5Ftf/MtHA1i+kjtzBITC0xAvmSXKzjN1YhtcS9rXyMHXBiFhXLdmvOXjkn0Okosr2+tmesXfSwDGhH3ZlOdHzit4D602RNl0nTA1dOUWHuCncs1TrWbriax86P/EYvmzMiHWCVTmmNJC0bMQKBgQD0HAXKNsYsdjCQOV4t3SMqOKaul67x/KA20PmMZVfQ2sQkyjyFgWpL8C16Rzf3zI7df+zF5SkvhFY4+LRZVwX5okEFYTzAZ/NYouj1/DABYOPq0E0sY18/xtq7FJ/CIk8qmCqcczqoyaoxoaC1zAt9E4CYE89iEOnO+GhcI3H3LwKBgQDFlQzvbXhWRyRFkeft/a52XLnyj6t9iP7wNGbGCSeoMDrAu3ZgoqacUPWj5MgSFZdT48H9rF4pPixXoe3jfUNsWBUHqD1F2drDz7lpL0PbpSsgy6ei+D4RwTADsuyXwrkvrWrGro+h6pNJFyly3nea/gloDtJTzfhFFwtNfmqyCQKBgBXzMx4UwMscsY82aV6MZO4V+/71CrkdszZaoiXaswPHuB1qxfhnQ6yiYyR8pO62SR5ns120Fnj8WFh1HJpv9cyVp20ZakIO1tXgiDweOh7VnIjvxBC6usTcV6y81QS62w2Ec0hwIBUvVQtzciUGvP25NDX4igxSYwPGWHP4h/XnAoGAcQN2aKTnBgKfPqPcU4ac+drECXggESgBGof+mRu3cT5U/NS9Oz0Nq6+rMVm1DpMHAdbuqRikq1aCqoVWup51qE0hikWy9ndL6GCynvWIDOSGrLWQZ2kyp5kmy5bWOWAJ6Ll6r7Y9NdIk+NOkw614IFFaNAj2STUw4uPxdRvwD3ECgYEArwOZxR3zl/FZfsvVCXfK8/fhuZXMOp6Huwqky4tNpVLvOyihpOJOcIFj6ZJhoVdmiL8p1/1S+Sm/75gx1tpFurKMNcmYZbisEC7Ukx7RQohZhZTqMPgizlVBTu5nR3xkheaJC9odvyjrWQJ569efXo30gkW04aBp7A15VNG5Z/U=";

        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkWs+3gXMosiWG+EbfRyotWB0waqU3t7qMQSBxU0r3JZoND53jvWQfzrGZ8W+obMc+OgwupODDVxhG/DEKVBIptuUQYdvAjCSH98m2hclFcksspuCy9xS7PyflPE47pVzS6vA3Slvw5OFQ2qUcku4paWnBxguLUGPjEncij5NcyFyk+/k57MmrVJwCZaI+lFOS3Eq2IXc07tWXO4s/2SWr3EJiwJutOGBdA1ddvv1Urrl0pWpEFg30pJB6J7YteuxdEL90kuO5ed/vnTK5qgQRvEelROkUW44xONk1784v28OJXmGICmNL1+KyM/SFbFOSgJZSV1tEXUzvL/xvzFpLwIDAQAB";

        // 返回给页面的是一个支付宝的表单
        String form = "";

        // 调用支付宝接口
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2021001163617452", privateKey, "json", "GBK", publicKey, "RSA2");
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"20220324331231211001\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "\"total_amount\":88.88," +
                "\"subject\":\"Iphone6 16G\"}");
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            form = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }
}
