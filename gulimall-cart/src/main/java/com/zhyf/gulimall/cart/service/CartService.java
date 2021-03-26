package com.zhyf.gulimall.cart.service;

import com.zhyf.gulimall.cart.vo.CartItem;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num);
}
