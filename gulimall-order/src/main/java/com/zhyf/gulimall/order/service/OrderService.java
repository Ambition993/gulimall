package com.zhyf.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.gulimall.order.entity.OrderEntity;
import com.zhyf.gulimall.order.vo.OrderConfirmVo;
import com.zhyf.gulimall.order.vo.OrderSubmitVo;
import com.zhyf.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 13:25:02
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);
}

