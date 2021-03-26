package com.zhyf.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.cart.feign.ProductFeignService;
import com.zhyf.gulimall.cart.interceptor.CartInterceptor;
import com.zhyf.gulimall.cart.service.CartService;
import com.zhyf.gulimall.cart.to.UserInfoTo;
import com.zhyf.gulimall.cart.vo.CartItem;
import com.zhyf.gulimall.cart.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;
    public static final String CART_PREFIX = "gulimall:cart";

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        // 商品添加到购物车
        CartItem cartItem = new CartItem();
        // 远程查询当前要添加的商品的信息
        CompletableFuture<Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
            R skuInfo = productFeignService.getSkuInfo(skuId);
            SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
            });
            cartItem.setChecked(true);
            cartItem.setCount(num);
            cartItem.setImage(data.getSkuDefaultImg());
            cartItem.setTitle(data.getSkuTitle());
            cartItem.setSkuId(data.getSkuId());
            cartItem.setPrice(data.getPrice());
        }, executor);
        // 远程查询sku的组合信息
        CompletableFuture<Void> getSkuAtteSaleValuesFuture = CompletableFuture.runAsync(() -> {
            List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
            cartItem.setSkuAttr(attrValues);
        }, executor);
        String cartString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), cartString);
        return cartItem;
    }


    /**
     * 获取我们将要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }
}
