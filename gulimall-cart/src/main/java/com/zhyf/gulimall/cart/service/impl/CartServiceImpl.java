package com.zhyf.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.cart.feign.ProductFeignService;
import com.zhyf.gulimall.cart.interceptor.CartInterceptor;
import com.zhyf.gulimall.cart.service.CartService;
import com.zhyf.gulimall.cart.to.UserInfoTo;
import com.zhyf.gulimall.cart.vo.Cart;
import com.zhyf.gulimall.cart.vo.CartItem;
import com.zhyf.gulimall.cart.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;
    public static final String CART_PREFIX = "gulimall:cart:";

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            // 购物车无此商品
            // 添加新商品到购物车
            CartItem cartItem = new CartItem();
            // 1 远程查询当前要添加的商品的信息
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
            // 2 远程查询sku的组合信息
            CompletableFuture<Void> getSkuAtteSaleValuesFuture = CompletableFuture.runAsync(() -> {
                List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(attrValues);
            }, executor);
            CompletableFuture.allOf(getSkuInfoFuture, getSkuAtteSaleValuesFuture).get();
            String cartString = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), cartString);
            return cartItem;
        } else {
            // 已经有这个商品只需要改一下数量即可
            CartItem item = JSON.parseObject(res, CartItem.class);
            item.setCount(item.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(item));
            return item;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String itemString = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(itemString, CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        // 判断是不是已经登录的状态
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            // 获取临时购物车里面的商品项
            List<CartItem> tempItems = getCartItems(CART_PREFIX + userInfoTo.getUserKey());
            if (tempItems != null && tempItems.size() > 0) {
                // 有数据需要合并
                for (CartItem tempItem : tempItems) {
                    addToCart(tempItem.getSkuId(), tempItem.getCount());
                }
                // 清空临时购物车
                clearCart(CART_PREFIX + userInfoTo.getUserKey());
            }
            // 获取登录后的购物车  包括合并过来的购物车以及临时购物车
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获得临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setChecked(check == 1);
        String itemString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), itemString);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String itemString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), itemString);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
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

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> cartItems = values.stream().map(obj -> {
                String str = (String) obj;
                return JSON.parseObject(str, CartItem.class);
            }).collect(Collectors.toList());
            return cartItems;
        }
        return null;
    }

}
