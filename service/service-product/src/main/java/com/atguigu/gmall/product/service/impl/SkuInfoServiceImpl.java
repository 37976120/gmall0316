package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.commom.constant.RedisConst;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.list.client.ListFeign;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.xml.transform.Source;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {
    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ListFeign listFeign;

    @Override
    public IPage<SkuInfo> list(Page<SkuInfo> skuInfoPage) {
        return skuInfoMapper.selectPage(skuInfoPage, null);
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        skuInfoMapper.insert(skuInfo);
        //添加Sku中的BaseAttrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insert(skuAttrValue);
        }
        //添加sku中的skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insert(skuImage);
        }
        //添加sku中的skuSaleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }
    }

    //使用AOP实现
    @GmallCache(prefix = RedisConst.SKUKEY_PREFIX)//指定这个被代理对象需要的前缀
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        return getSkuInfoFromDB(skuId);
    }

    //添加redis缓存机制-->没用AOP
    public SkuInfo getSkuInfoFormCache(String skuId) {
        long s = System.currentTimeMillis();
        //skuInfo对象
        SkuInfo skuInfo = null;
        //同步用key值
        String redisKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        //锁的key值
        String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
        //从redis中取
        String redisValue = (String) redisTemplate.opsForValue().get(redisKey);
        /**没有则从数据库中取再存redis
         * key-->sku:id:skuInfo
         * value-->
         */
        if (StringUtils.isBlank(redisValue)) {
            //取锁
            String lockValue = UUID.randomUUID().toString();
            Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
            if (lock) {
                skuInfo = getSkuInfoFromDB(skuId);
                //空校验(防止恶意空值请求),同步缓存
                if (null != skuInfo) {
                    redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(skuInfo));
                } else {
                    redisTemplate.opsForValue().set(redisKey, "null", 10, TimeUnit.SECONDS);
                }
            } else {
                //自旋
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuInfoFormCache(skuId);
            }
            // 声明script--lua 脚本
            /*
            if redis.call("get",KEYS[1]) == ARGV[1]
            then
                return redis.call("del",KEYS[1])
            else
                return 0
            end
             */
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            // 设置lua脚本返回的数据类型
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            // 设置lua脚本返回类型为Long
            redisScript.setResultType(Long.class);
            redisScript.setScriptText(script);
            redisTemplate.execute(redisScript, Arrays.asList(lockKey), lockValue);

            //还锁
//            String LockValueFromCache = (String) redisTemplate.opsForValue().get(lockKey);
//            if (StringUtils.isNoneBlank(LockValueFromCache) && lockValue.equals(LockValueFromCache)) {
//                redisTemplate.delete(lockKey);
//            }
        } else {
            skuInfo = JSON.parseObject(redisValue, SkuInfo.class);
        }
        System.out.println("查询耗时" + (System.currentTimeMillis() - s));
        return skuInfo;
    }

    private SkuInfo getSkuInfoFromDB(String skuId) {
        QueryWrapper<SkuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id", skuId);
        List<SkuImage> skuImages = skuImageMapper.selectList(wrapper);
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        skuInfo.setSkuImageList(skuImages);
        return skuInfo;
    }

    @Override
    public BigDecimal getPrice(String skuid) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuid);
        BigDecimal price = skuInfo.getPrice();
        if (null == price) {
            price = new BigDecimal(0);
        }
        return price;
    }

    @Override
    public Map<String, String> getSkuValueIdsMap(Long spuId) {
        List<Map<String, Object>> collection = skuAttrValueMapper.getSkuValueIdsMap(spuId);
        HashMap<String, String> valuesId = new HashMap<>();
        for (Map<String, Object> map : collection) {
            String sku_id = map.get("sku_id").toString();
            String value_ids = map.get("value_ids").toString();
            valuesId.put(value_ids, sku_id);
        }
        return valuesId;//todo
    }

    @Override
    public void onSale(Long skuid) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(1);
        skuInfo.setId(skuid);
        skuInfoMapper.updateById(skuInfo);
        //上架
        listFeign.onSale(skuid);
    }

    @Override
    public void cancelSale(Long skuid) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(0);
        skuInfo.setId(skuid);
        skuInfoMapper.updateById(skuInfo);
        //下架
        listFeign.cancelSale(skuid);
    }
}

