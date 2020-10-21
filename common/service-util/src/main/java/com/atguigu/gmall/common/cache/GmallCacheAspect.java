package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.commom.constant.RedisConst;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {
    //前缀可以从标记代理对象的自定义注解用反射获取
    @Autowired
    RedisTemplate redisTemplate;

    // 把通知绑定到自定义注解上，直接放注解
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);
        String prefix = annotation.prefix();
        Object[] args = point.getArgs();
        Object result = null;
        String redisValue = null;

        String redisKey = prefix + args[0] + RedisConst.SKUKEY_SUFFIX;

        //查询缓存
        redisValue = (String) redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isBlank(redisValue)) {
            String lockKey = prefix + args[0] + RedisConst.SKULOCK_SUFFIX;
            String lockValue = UUID.randomUUID().toString();
            //  String lockValue = "666";
            //查询数据库前取锁
            Boolean allow = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
            System.out.println("allow" + allow);
            while (!allow) {
                //
                System.out.println("自旋中");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                allow = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
            }
            try {
                //执行被代理方法
                result = point.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            if (result != null) {
                redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(result));
            } else {
                redisTemplate.opsForValue().setIfAbsent(redisKey, "null", 10, TimeUnit.SECONDS);
            }
            //还锁
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            // 设置lua脚本返回的数据类型
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            // 设置lua脚本返回类型为Long
            redisScript.setResultType(Long.class);
            redisScript.setScriptText(script);
            redisTemplate.execute(redisScript, Arrays.asList(lockKey), lockValue);
        } else {
            //返回数据
            result = JSON.parseObject(redisValue, signature.getReturnType());
        }
        return result;
    }
}
