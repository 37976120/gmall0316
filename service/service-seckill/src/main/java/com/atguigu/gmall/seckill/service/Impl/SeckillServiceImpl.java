package com.atguigu.gmall.seckill.service.Impl;

import com.atguigu.gmall.commom.constant.RedisConst;
import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.commom.result.ResultCodeEnum;
import com.atguigu.gmall.commom.util.MD5;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.seckill.service.SeckillService;
import com.atguigu.gmall.seckill.service.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.seckill.util.CacheHelper;
import com.atguigu.gmall.service.RabbitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitService rabbitService;

    @Override
    public void putGoods() {
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(null);
        seckillGoods.stream().forEach(secGood -> {
            // 放入redis秒杀商品列表，hash
//            redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS, secGood.getSkuId() + "", secGood);
            redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(secGood.getSkuId() + "", secGood);
            for (int i = 0; i < secGood.getNum(); i++) {
                // 生成秒杀商品库存，list
                // 生成秒杀商品库存，list
                redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + secGood.getSkuId()).leftPush(secGood.getSkuId() + "");
//                redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX + secGood.getSkuId(), secGood.getSkuId());
            }
            // 发布通知消息，通知全体微服务秒杀入库
            redisTemplate.convertAndSend("seckillpush", secGood.getSkuId() + ":1");
        });
    }

    @Override
    public List<SeckillGoods> findAll() {
        List<SeckillGoods> values = (List<SeckillGoods>) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        return values;
    }

    @Override
    public Result getitem(String skuId) {
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId);
        return Result.ok(seckillGoods);
    }

    @Override
    public void seckillOrder(String skuId, String userId) {
        //防止用户并发抢
        Boolean first = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, 1, 10, TimeUnit.SECONDS);
        if (first) {
            // 发送抢单的消息队列
            // MessageSeckillUserRecode
            UserRecode messageSeckillUserRecode = new UserRecode();
            messageSeckillUserRecode.setSkuId(Long.parseLong(skuId));
            messageSeckillUserRecode.setUserId(userId);
            rabbitService.send(MqConst.EXCHANGE_DIRECT_SECKILL_USER, MqConst.ROUTING_SECKILL_USER, messageSeckillUserRecode);
        }
    }

    @Override
    public void consumeSeckillOrder(Long skuId, String userId) {
        String killRs = (String) redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).rightPop();
        //失败！发布库存空
        if (StringUtils.isBlank(killRs)) {
            redisTemplate.convertAndSend("seckillpush", skuId + "0");
        }
        // 成功，则生成预订单
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setSeckillGoods(this.getSeckillGoods(skuId));
        orderRecode.setNum(1);
        orderRecode.setOrderStr(MD5.encrypt(skuId + userId));//生成下单码
        //订单数据存入Reids
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(orderRecode.getUserId(), orderRecode);
    }

    @Override
    public Result checkOrder(String skuId, String userId) {
        //售罄状态
        String o = (String) CacheHelper.get(skuId + "");
        if ("0".equals(o)) {
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }
        //有预订单状态
        Boolean isPreOrder = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);
        if (isPreOrder) {
            return Result.build(null, ResultCodeEnum.SECKILL_SUCCESS);
        }
        //有正式订单:
        // 正式订单的保存方式redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId,orderId);
        boolean hasOrderId = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId);
        if (hasOrderId) {
            String orderId = (String) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);
            return Result.build(orderId, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }

        return null;
    }

    private SeckillGoods getSeckillGoods(Long skuId) {
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId + "");
        return seckillGoods;
    }
}
