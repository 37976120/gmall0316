package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CartController {

    @Autowired
    CartFeignClient cartFeignClient;

    //addCart.html?skuId=1&skuNum=1
    @RequestMapping("addCart.html")
    public String addCart(HttpServletRequest request, @RequestParam("skuId") Long skuId, @RequestParam("skuNum") Integer skuNum) {
        String userId = "";
        userId = request.getHeader("userTempId");
        if (StringUtils.isNotBlank(request.getHeader("userId"))) {
            userId = request.getHeader("userId");
        }
        if (StringUtils.isNotBlank(userId)) {
            //添加到购物车功能
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setSkuNum(skuNum);
            cartFeignClient.addCart(cartInfo);
        }
        return "redirect:http://cart.gmall.com/cart/addCart.html";
    }

    @RequestMapping("cart.html")//访问购物车列表
    public String cartHtml(HttpServletRequest request) {
        //购物车列表接口
        return "cart/index";//跳转购物车列表
    }

}
