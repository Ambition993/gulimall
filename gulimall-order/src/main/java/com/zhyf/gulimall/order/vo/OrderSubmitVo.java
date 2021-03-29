package com.zhyf.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装订单提交
 */
@Data
public class OrderSubmitVo {
    private Long addrId;  //收货地址ID
    private Integer payType; // 支付方式
    // 无需提交购买的商品 去购物车获取一次
    // 优惠发票
    private String orderToken;  //令牌
    private BigDecimal payPrice; // 应付价格
    //用户相关信息 直接去session取出登录的用户
    private String notes;   // 备注;
}
