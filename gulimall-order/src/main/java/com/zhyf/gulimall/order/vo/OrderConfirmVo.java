package com.zhyf.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    //防重令牌
    @Setter@Getter
    String orderToken;
    @Getter
    @Setter
    Map<Long, Boolean> stocks;


    public Integer getCount() {
        Integer count = 0;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }


    /**
     * 订单总额
     **/
    //BigDecimal total;
    //计算订单总额
    public BigDecimal getTotal() {
        BigDecimal totalNum = BigDecimal.ZERO;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                //计算当前商品的总价格
                BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                //再计算全部商品的总价格
                totalNum = totalNum.add(itemPrice);
            }
        }
        return totalNum;
    }


    /**
     * 应付价格
     **/
    //BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
