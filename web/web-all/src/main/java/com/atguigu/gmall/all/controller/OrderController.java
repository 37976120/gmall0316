package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderController {
    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    OrderFeignClient orderFeignClient;

    @RequestMapping("trade.html")
    public String trade(HttpServletRequest request, Model model) {
        //能到此页面只有已经登录
        String userId = request.getHeader("userId");

        //获得购物车数据
        List<CartInfo> cartInfoList = cartFeignClient.getCheckedCartList(userId);
        //生成订单页面数据
        List<OrderDetail> orderDetails = cartInfoList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            return orderDetail;
        }).collect(Collectors.toList());

        //获取地址
        List<UserAddress> addressList = userFeignClient.findUserAddressListByUserId(userId);

        model.addAttribute("detailArrayList", orderDetails);
        model.addAttribute("userAddressList", addressList);
        model.addAttribute("totalAmount", getTotalAmount(cartInfoList));
        //可用于防止重复提交
        model.addAttribute("tradeNo", orderFeignClient.genTradeNo(userId));

        return "order/trade";
    }

    private BigDecimal getTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal bigDecimal = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            bigDecimal = bigDecimal.add(cartInfo.getCartPrice());
        }
        return bigDecimal;
    }
}
