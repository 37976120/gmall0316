package com.atguigu.gmall.cart.service.Impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.commom.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ProductFeignClient productFeignClient;

    @Override
    public void addCart(CartInfo cartInfo) {
        // 检索当前购物车数据是否添加过
        Long skuId = cartInfo.getSkuId();
        String userId = cartInfo.getUserId();
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("sku_id", skuId);
        cartInfoQueryWrapper.eq("user_id", userId);
        CartInfo cartInfoAdd = cartInfoMapper.selectOne(cartInfoQueryWrapper);
        //新增购物车中没有的物品
        if (null == cartInfoAdd) {
            cartInfoMapper.insert(cartInfo);
            // 同步缓存
            redisTemplate.boundHashOps(RedisConst.USER_KEY_PREFIX + cartInfo.getUserId() + RedisConst.USER_CART_KEY_SUFFIX).put(cartInfo.getSkuId() + "", cartInfo);
        } else {
            //添加购物车中已存在的为增加数量，赋时对缓存中的数据也要修改
            Integer skuNum = cartInfo.getSkuNum();
            Integer skuNumAdd = cartInfoAdd.getSkuNum();
            // skuPrice是实时字段，从传入的参数中获取
            cartInfoAdd.setSkuPrice(cartInfo.getSkuPrice());
            //数量为原数+传的数
            cartInfoAdd.setSkuNum(skuNum + skuNumAdd);
            cartInfoAdd.setCartPrice(cartInfoAdd.getSkuPrice().multiply(new BigDecimal(cartInfoAdd.getSkuNum())));
            cartInfoMapper.updateById(cartInfoAdd);
            // 同步缓存，只是和添加时的value不同 即为修改
            redisTemplate.boundHashOps(RedisConst.USER_KEY_PREFIX + cartInfo.getUserId() + RedisConst.USER_CART_KEY_SUFFIX).put(cartInfo.getSkuId() + "", cartInfoAdd);
        }
    }

    @Override
    public List<CartInfo> cartList(String userId) {
        //先查缓存
        List<CartInfo> cartInfos = null;
        cartInfos = (List<CartInfo>) redisTemplate.opsForHash().values(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX);
        //没有再去查DB
        if (cartInfos == null || cartInfos.size() == 0) {
            cartInfos = getCartInfosFromDbAndCache(userId);
        }
        return cartInfos;
    }

    private List<CartInfo> getCartInfosFromDbAndCache(String userId) {
        List<CartInfo> cartInfos;
        QueryWrapper<CartInfo> cartInfoWrapper = new QueryWrapper<>();
        cartInfoWrapper.eq("user_id", userId);
        cartInfos = cartInfoMapper.selectList(cartInfoWrapper);

        //同步缓存
        if (cartInfos != null || cartInfos.size() > 0) {
            HashMap<String, Object> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
                BigDecimal price = productFeignClient.getPrice(cartInfo.getSkuId() + "");
                cartInfo.setSkuPrice(price);
                map.put(cartInfo.getSkuId() + "", cartInfo);
            }
            redisTemplate.opsForHash().putAll(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX, map);
        }
        return cartInfos;
    }

    //选中功能
    @Override
    public void checkCart(CartInfo cartInfo) {
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id", cartInfo.getSkuId());
        wrapper.eq("user_id", cartInfo.getUserId());
        cartInfoMapper.update(cartInfo, wrapper);
        getCartInfosFromDbAndCache(cartInfo.getUserId());
    }


}
