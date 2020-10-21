package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {
    @Autowired
    CartService cartService;

    @Autowired
    ProductFeignClient productFeignClient;

    @RequestMapping("addCart")
    void addCart(@RequestBody CartInfo cartInfo, HttpServletRequest request) {
        // 获取到通过网关验证获得的userId
        String userId = "";
        userId = request.getHeader("userTempId");
        if (!StringUtils.isEmpty(request.getHeader("userId"))) {
            userId = request.getHeader("userId");
        }
        Long skuId = cartInfo.getSkuId();
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId.toString());
        //从数据库查出购物车数据封装POJO
        cartInfo.setUserId(userId);
        cartInfo.setIsChecked(1);
        cartInfo.setSkuPrice(skuInfo.getPrice());//数据库中不存在，提供给缓存
        cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuName(skuInfo.getSkuName());

        cartService.addCart(cartInfo);
    }

    //结算页面的异步请求
    @RequestMapping("cartList")
    Result cartList(HttpServletRequest request) {
        //取到userId
        String userId = "";
        userId = request.getHeader("userTempId");
        if (StringUtils.isBlank(userId)) {
            userId = request.getHeader("userId");
        }
        List<CartInfo> data = cartService.cartList(userId);
        return Result.ok(data);
    }

    //选中功能
    @RequestMapping("checkCart/{skuId}/{status}")
    Result checkCart(@PathVariable("skuId") String skuId, @PathVariable("status") Integer status, HttpServletRequest request) {
        String userId = "";
        userId = request.getHeader("userTempId");
        if (StringUtils.isBlank(userId)) {
            userId = request.getHeader("userId");
        }
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(Long.parseLong(skuId));
        cartInfo.setIsChecked(status);
        cartService.checkCart(cartInfo);
        return Result.ok();
    }

    @RequestMapping("inner/getCheckedCartList/{userId}")
    List<CartInfo> getCheckedCartList(@PathVariable("userId") String userId) {
        List<CartInfo> cartInfoList = cartService.cartList(userId);
        cartInfoList.removeIf(cartInfo -> cartInfo.getIsChecked().toString().equals("0"));
//        Iterator<CartInfo> iterator = cartInfoList.iterator();
//        while (iterator.hasNext()) {
//            Integer isChecked = iterator.next().getIsChecked();
//            if (iterator.next().getIsChecked().toString().equals("0")) {
//                iterator.remove();
//            }
//        }
        return cartInfoList;
    }

}
