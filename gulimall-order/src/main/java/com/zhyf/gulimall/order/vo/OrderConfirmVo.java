package com.zhyf.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

public class OrderConfirmVo {
    // 用户收货地址列表
    @Getter
    @Setter
    List<MemberAddressVo> address;

    // 所有选中的购物项
    @Getter
    @Setter
    List<OrderItemVo> items;

    // 发票信息

    // 优惠券
    @Getter
    @Setter
    Integer integration;
//    //订单总额
//    BigDecimal total;
    //应付价格
//    BigDecimal payPrice;

    //防重令牌
    String orderToken;

    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                total.add(multiply);
            }
        }
        return total;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
