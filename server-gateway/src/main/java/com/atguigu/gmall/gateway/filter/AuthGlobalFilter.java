package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.commom.result.ResultCodeEnum;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthGlobalFilter implements GlobalFilter {
    @Value("${authUrls.url}")
    String authUrls;
    @Autowired
    UserFeignClient userFeignClient;

    /**
     * 放行
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().toString();
        String uri = request.getURI().toString();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        // 不拦截认证中心的请求
        if (uri.indexOf("passport") != -1 || uri.indexOf("ico") != -1 || uri.indexOf("css") != -1 || uri.indexOf("js") != -1 || uri.indexOf("png") != -1 || uri.indexOf("jpg") != -1) {
            return chain.filter(exchange);
        }
        //禁所有外部的inner请求
        boolean matchInner = antPathMatcher.match("/api/**/inner/**", path);
        if (matchInner) {
            return out(response, ResultCodeEnum.PERMISSION);
        }
        //Token鉴权
        //主页详情页请求可能无token
        String userId = "";
        String token = getToken(request);
        if (StringUtils.isNotBlank(token)) {
            userId = userFeignClient.a();
            userId = userFeignClient.check(token);
        }

        //------------鉴权auth API访问(检查token) beg---------------
        boolean authMatch = antPathMatcher.match("/api/**/auth/**", path);
        if (authMatch) {
            //鉴权不通过
            if (StringUtils.isBlank(userId)) {
                return out(response, ResultCodeEnum.PERMISSION);
            }
        }
        //------------鉴权auth API访问(检查token) end---------------

        //------------拦截需登录页面 beg---------------
        if (authUrls != null) {
            String[] split = authUrls.split(",");
            for (String s1 : split) {
                if (path.indexOf(s1) != -1) {
                    //鉴权不通过
                    if (StringUtils.isBlank(userId)) {
                        // 重定向到登录页面
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.gmall.com/login.html?originUrl=" + uri);
                        Mono<Void> voidMono = response.setComplete();
                        return voidMono;
                    }
                }
            }
        }
        //------------拦截需登录页面 end---------------

        //传递ID值
        if (!StringUtils.isEmpty(userId)) {
            request.mutate().header("userId", userId).build();// 刷新request放入userId
            exchange.mutate().request(request).build();//刷新exchange放入userId
        } else {
            request.mutate().header("userTempId", getUserTempId(request)).build();
            exchange.mutate().request(request).build();
        }
        return chain.filter(exchange);//放行
//        return out(response, ResultCodeEnum.SECKILL_END);//打回
    }

    private String getUserTempId(ServerHttpRequest request) {
        String userTempIdValue = "";
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies != null) {
            HttpCookie userTempId = cookies.getFirst("userTempId");
            if (userTempId != null) {
                userTempIdValue = userTempId.getValue();
            }
        }

        // ajax异步访问
        if (StringUtils.isEmpty(userTempIdValue)) {
            userTempIdValue = request.getHeaders().getFirst("userTempId");
        }
        return userTempIdValue;
    }

    private String getToken(ServerHttpRequest request) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        String token = "";
        if (cookies != null) {
            HttpCookie token1 = cookies.getFirst("token");
            if (token1 != null) {
                token = token1.getValue();
            }
        }
        //异步请求token被放在头中
        if (StringUtils.isBlank(token)) {
            token = request.getHeaders().getFirst("token");
        }
        return token;
    }

    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // 返回用户没有权限登录
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 输出到页面
        return response.writeWith(Mono.just(wrap));
    }
}
