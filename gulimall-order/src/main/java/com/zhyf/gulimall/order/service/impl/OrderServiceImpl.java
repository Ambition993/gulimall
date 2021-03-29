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
import com.zhyf.gulimall.order.feign.WmsFeignService;
import com.zhyf.gulimall.order.interceptor.LoginUserInterceptor;
import com.zhyf.gulimall.order.service.OrderService;
import com.zhyf.gulimall.order.vo.MemberAddressVo;
import com.zhyf.gulimall.order.vo.OrderConfirmVo;
import com.zhyf.gulimall.order.vo.OrderItemVo;
import com.zhyf.gulimall.order.vo.SkuStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberTo memberTo = LoginUserInterceptor.toThreadLocal.get();
        // 給子線程共享數據
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 收货地址列表
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberTo.getId());
            confirmVo.setAddress(address);
        }, executor);
        // 远程查询购物车数据
        CompletableFuture<Void> getCartItemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(cartItems);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            List<SkuStockVo> data = wmsFeignService.getSkusHasStock(skuIds);
            if (data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        }, executor);
        // 用户积分
        Integer integration = memberTo.getIntegration();
        confirmVo.setIntegration(integration);
        // 其他数据自动计算
        CompletableFuture.allOf(getAddressFuture, getCartItemsFuture).get();
        return confirmVo;
    }

}