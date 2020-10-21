package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.commom.constant.RedisConst;
import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.commom.result.ResultCodeEnum;
import com.atguigu.gmall.commom.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.seckill.service.SeckillService;
import com.atguigu.gmall.seckill.util.CacheHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillApiController {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    SeckillService seckillService;

    @Autowired
    OrderFeignClient orderFeignClient;

    @RequestMapping("pop")
    public Result pop() {
        String o = (String) CacheHelper.get("30");
        return Result.ok();
    }

    @RequestMapping("putGoods")
    public Result putGoods() {
        seckillService.putGoods();
        return Result.ok();
    }

    @RequestMapping("findAll")
    Result findAll() {
        List<SeckillGoods> data = seckillService.findAll();
        return Result.ok(data);
    }

    @RequestMapping("getItem/{skuId}")
    Result getItem(@PathVariable("skuId") String skuId) {
        Result data = seckillService.getitem(skuId);
        return data;
    }

    @RequestMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable("skuId") String skuId, HttpServletRequest request) {
        String userId = request.getHeader("userId");
        String encrypt = MD5.encrypt(skuId + userId);
        return Result.ok(encrypt);
    }

    @RequestMapping("/auth/seckillOrder/{skuId}")
    Result seckillOrder(@PathVariable("skuId") String skuId, String skuIdStr, HttpServletRequest request) {
        String userId = request.getHeader("userId");
        //校验:非法或售罄
        if (!MD5.encrypt(skuId + userId).equals(skuIdStr)) {
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        if ("0".equals(CacheHelper.get(skuId))) {
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }
        seckillService.seckillOrder(skuId, userId);
        return Result.ok();
    }


    @RequestMapping("/auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") String skuId, HttpServletRequest request) {
        String userId = request.getHeader("userId");
        Result data = seckillService.checkOrder(skuId, userId);
        return data;
    }

    @RequestMapping("auth/getPreOrder/{userId}")
    OrderRecode getPreOrder(@PathVariable("userId") String userId) {
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        return orderRecode;
    }

    /**
     * 秒杀保存订单的异步调用
     *
     * @param orderInfoFromPage
     */
    @RequestMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfoFromPage, HttpServletRequest request) {
        String userId = request.getHeader("userId");
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();

        // 保存订单信息(订单表和订单详情表)
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
        orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
        orderInfo.setTotalAmount(seckillGoods.getCostPrice());
        orderInfo.setOrderComment("快点");
        orderInfo.setPaymentWay(PaymentWay.ONLINE.getComment());
        orderInfo.setCreateTime(new Date());
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, 1);
        Date expireTime = instance.getTime();
        orderInfo.setExpireTime(expireTime);// 当前时间new Date()基础上+1天，过期时间-当前时间=倒计时
        orderInfo.setUserId(Long.parseLong(userId));
        orderInfo.setConsigneeTel(orderInfoFromPage.getConsigneeTel());
        orderInfo.setConsignee(orderInfoFromPage.getConsignee());
        orderInfo.setDeliveryAddress(orderInfoFromPage.getDeliveryAddress());
        orderInfo.setImgUrl(seckillGoods.getSkuDefaultImg());
        // 生成系统外部订单号，用来和支付宝进行交易
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeFormat = sdf.format(new Date());
        long currentTimeMillis = System.currentTimeMillis();
        String outTradeNo = "atguigu" + currentTimeMillis + timeFormat;
        orderInfo.setOutTradeNo(outTradeNo);//"atguigu"+毫秒时间戳+时间格式化字符串
        //封装子属性orderDetail
        List<OrderDetail> orderDetails = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        BeanUtils.copyProperties(seckillGoods, orderDetail);
        orderDetails.add(orderDetail);
        orderInfo.setOrderDetailList(orderDetails);

        OrderInfo orderInfoed = orderFeignClient.saveSeckillOrder(orderInfo);
        //最后返回生成的订单号
        return Result.ok(orderInfoed.getId());
    }
}
