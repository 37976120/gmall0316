package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.commom.util.HttpClient;
import com.atguigu.gmall.commom.util.HttpClientUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @RequestMapping("inner/getOrderById/{orderId}")
    OrderInfo getOrderById(@PathVariable("orderId") String orderId) {
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        return orderInfo;
    }

    /**
     * 保存：购物车数据  地址数据  order_info order_detail
     *
     * @param tradeNo
     * @param order
     * @return
     */
    @RequestMapping("auth/submitOrder")
    public Result submitOrder(@RequestParam("tradeNo") String tradeNo, @RequestBody OrderInfo order, HttpServletRequest request) {
        String userId = request.getHeader("userId");

        //校验页面交易码（防止重复提交）
        boolean checkTradeNo = orderService.checkTradeNo(tradeNo, userId);
        if (!checkTradeNo) {
            return Result.fail();
        }

        String consignee = order.getConsignee();
        String deliveryAddress = order.getDeliveryAddress();
        String consigneeTel = order.getConsigneeTel();

        List<CartInfo> checkedCartList = cartFeignClient.getCheckedCartList(userId);
        if (null != checkedCartList || checkedCartList.size() > 0) {
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());//
            orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());//
            orderInfo.setTotalAmount(getTotalAmount(checkedCartList));//
            orderInfo.setOrderComment("快点发货");
            orderInfo.setPaymentWay(PaymentWay.ONLINE.getComment());//支付方式
            orderInfo.setCreateTime(new Date());

            Calendar instance = Calendar.getInstance();
            instance.add(Calendar.DATE, 1);
            Date time = instance.getTime();
            //订单有效期为一天
            orderInfo.setExpireTime(time);//过期时间
            orderInfo.setUserId(Long.parseLong(userId));
            orderInfo.setConsigneeTel(consigneeTel);//收货人电话
            orderInfo.setConsignee(consignee);//收货人
            orderInfo.setDeliveryAddress(deliveryAddress);//地址ID
            orderInfo.setImgUrl(checkedCartList.get(0).getImgUrl());
            // 生成系统外部订单号，用来和支付宝进行交易
            //当前时期+当前毫秒时间+id
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(new Date());
            long l = System.currentTimeMillis();

            String outTradeNo = "atguigu" + l + format;
            orderInfo.setOutTradeNo(outTradeNo);

            ArrayList<OrderDetail> orderDetails = new ArrayList<>();
            for (CartInfo cartInfo : checkedCartList) {
                OrderDetail orderDetail = new OrderDetail();
                BeanUtils.copyProperties(cartInfo, orderDetail);
                orderDetail.setOrderPrice(cartInfo.getCartPrice());

                // 校验此时的真实价格(调用product系统),webservice
                BigDecimal price = productFeignClient.getPrice(cartInfo.getSkuId().toString());
                int rs = price.compareTo(new BigDecimal("0"));
                if (rs == 0) {
                    return Result.fail();
                }

//                // 校验此时的真实库存(调用库存系统) //todo 跳过库存检查
//                String stockStatus = HttpClientUtil.doGet("http://localhost:9001/hasStock?skuId=" + cartInfo.getSkuId() + "&num=" + cartInfo.getSkuNum());
//                if (StringUtils.isNotBlank(stockStatus)) {
//                    int i = new BigDecimal(stockStatus).compareTo(new BigDecimal("0"));
//                    if (i == 0) {
//                        return Result.fail();
//                    }
//                }

                orderDetails.add(orderDetail);
            }

            orderInfo.setOrderDetailList(orderDetails);
            OrderInfo orderInfoSave = orderService.save(orderInfo);
            //todo 提交订单后删除购物车数据
            return Result.ok(orderInfoSave.getId());

        }
        return Result.fail();
    }

    private BigDecimal getTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal bigDecimal = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            bigDecimal = bigDecimal.add(cartInfo.getCartPrice());
        }
        return bigDecimal;
    }

    @RequestMapping("inner/genTradeNo/{userId}")
    public String genTradeNo(@PathVariable("userId") String userId) {
        String tradeNo = orderService.genTradeNo(userId);
        return tradeNo;
    }

    /**
     * 保存秒杀页订单
     *
     * @param orderInfo
     */
    @RequestMapping("inner/saveSeckillOrder")
    OrderInfo saveSeckillOrder(@RequestBody OrderInfo orderInfo) {
        OrderInfo orderInfoed = orderService.save(orderInfo);
        return orderInfoed;
    }
}
