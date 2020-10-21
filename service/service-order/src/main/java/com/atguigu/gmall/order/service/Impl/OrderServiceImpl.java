package com.atguigu.gmall.order.service.Impl;

import com.atguigu.gmall.commom.constant.RedisConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Override
    public String genTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(RedisConst.USER_KEY_PREFIX + userId + ":tradeNo", tradeNo);
        return tradeNo;
    }

    /**
     * 保存 order_info  order_detail
     *
     * @param orderInfo
     * @return
     */
    @Override
    public OrderInfo save(OrderInfo orderInfo) {
        orderInfoMapper.insert(orderInfo);
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            //关联order_info的orderId
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }
        return orderInfo;
    }

    @Override
    public boolean checkTradeNo(String tradeNo, String userid) {
        boolean b = false;
        String o = (String) redisTemplate.opsForValue().get(RedisConst.USER_KEY_PREFIX + userid + ":tradeNo");
        if (tradeNo.equals(o)) {
            b = true;
            redisTemplate.delete(RedisConst.USER_KEY_PREFIX + userid + ":tradeNo");
        }
        return b;
    }

    @Override
    public OrderInfo getOrderById(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        QueryWrapper<OrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

    @Override
    public void updateOrderStatus(String out_trade_no) {
        OrderInfo orderInfoForUpdate = new OrderInfo();
        orderInfoForUpdate.setProcessStatus(ProcessStatus.PAID.getComment());
        orderInfoForUpdate.setOrderStatus(OrderStatus.PAID.getComment());
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no", out_trade_no);
        orderInfoMapper.update(orderInfoForUpdate, wrapper);

        OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
        QueryWrapper<OrderDetail> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("order_id", orderInfo.getId());
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper1);
        orderInfo.setOrderDetailList(orderDetails);

        // message 需要的是WareOrderTask的json对象
        // orderInfo - > WareOrderTask
    }
}
