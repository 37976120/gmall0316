package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.seckill.client.SeckillFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class seckillController {

    @Autowired
    SeckillFeignClient seckillFeignClient;

    @Autowired
    UserFeignClient userFeignClient;


    /**
     * 秒杀页面入口
     * <p>
     * 秒杀的内部调用都是返回result，因为有个状态
     */
    @RequestMapping("seckill.html")
    public String index(Model model) {
        Result result = seckillFeignClient.findAll();
        model.addAttribute("list", result.getData());
        return "seckill/index";
    }

    @RequestMapping("seckill/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) {
        Result result = seckillFeignClient.getItem(skuId.toString());
        model.addAttribute("item", result.getData());
        return "seckill/item";
    }

    @RequestMapping("seckill/queue.html")
    public String queue(String skuId, String skuIdStr, HttpServletRequest request) {
        request.setAttribute("skuId", skuId);
        request.setAttribute("skuIdStr", skuIdStr);
        return "seckill/queue";
    }

    @RequestMapping("seckill/trade.html")
    public String trade(HttpServletRequest request, Model model) {
        String userId = request.getHeader("userId");
        // 从预订单获得skuId
        OrderRecode orderRecode = seckillFeignClient.getPreOrder(userId);
        Long skuId = orderRecode.getSeckillGoods().getSkuId();
        List<UserAddress> userAddressListByUserId = userFeignClient.findUserAddressListByUserId(userId);
        //秒杀商品详情，从缓存,虽然只有一个也得用List
        List<OrderDetail> orderDetails = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        LinkedHashMap seckillGoods = (LinkedHashMap) seckillFeignClient.getItem(skuId.toString()).getData();
        orderDetail.setSkuName((String) seckillGoods.get("skuName"));
        String skuId1 = seckillGoods.get("skuId").toString();
        orderDetail.setSkuId(Long.parseLong(skuId1));
        orderDetail.setImgUrl((String) seckillGoods.get("skuDefaultImg"));
        orderDetail.setSkuNum(1);
        orderDetail.setOrderPrice(new BigDecimal(seckillGoods.get("costPrice").toString()));

        orderDetails.add(orderDetail);
        model.addAttribute("userAddressList", userAddressListByUserId);
        model.addAttribute("detailArrayList", orderDetails);
        model.addAttribute("totalAmount", seckillGoods.get("costPrice").toString());

        return "seckill/trade";
    }

}
