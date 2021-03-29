package com.zhyf.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.to.member.MemberTo;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.gulimall.order.dao.OrderDao;
import com.zhyf.gulimall.order.entity.OrderEntity;
import com.zhyf.gulimall.order.feign.CartFeignService;
import com.zhyf.gulimall.order.feign.MemberFeignService;
import com.zhyf.gulimall.order.interceptor.LoginUserInterceptor;
import com.zhyf.gulimall.order.service.OrderService;
import com.zhyf.gulimall.order.vo.MemberAddressVo;
import com.zhyf.gulimall.order.vo.OrderConfirmVo;
import com.zhyf.gulimall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberTo memberTo = LoginUserInterceptor.toThreadLocal.get();
        // 收货地址列表
        List<MemberAddressVo> address = memberFeignService.getAddress(memberTo.getId());
        confirmVo.setAddress(address);
        // 远程查询购物车数据
        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        confirmVo.setItems(cartItems);
        // 用户积分
        Integer integration = memberTo.getIntegration();
        confirmVo.setIntegration(integration);
        // 其他数据自动计算
        return confirmVo;
    }

}