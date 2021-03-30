package com.zhyf.gulimall.order.service.to;

import com.zhyf.gulimall.order.entity.OrderEntity;
import com.zhyf.gulimall.order.entity.OrderItemEntity;

import java.math.BigDecimal;
import java.util.List;

public class OrderCreateTo {
    private OrderEntity order; // 订单

    private List<OrderItemEntity> items; // 订单项

    private BigDecimal payPrice; // 应付价格

    private BigDecimal fare; // 运费
}
