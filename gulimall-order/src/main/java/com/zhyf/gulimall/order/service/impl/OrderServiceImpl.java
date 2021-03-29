package com.zhyf.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.to.member.MemberTo;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.gulimall.order.constant.OrderConstant;
import com.zhyf.gulimall.order.dao.OrderDao;
import com.zhyf.gulimall.order.entity.OrderEntity;
import com.zhyf.gulimall.order.feign.CartFeignService;
import com.zhyf.gulimall.order.feign.MemberFeignService;
import com.zhyf.gulimall.order.feign.WmsFeignService;
import com.zhyf.gulimall.order.interceptor.LoginUserInterceptor;
import com.zhyf.gulimall.order.service.OrderService;
import com.zhyf.gulimall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    StringRedisTemplate redisTemplate;

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

        //放重令牌
        String token = UUID.randomUUID().toString().replace("_", "");
        confirmVo.setOrderToken(token);
        // Redis 放入令牌
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberTo.getId(), token, 20, TimeUnit.MINUTES);
        CompletableFuture.allOf(getAddressFuture, getCartItemsFuture).get();
        return confirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();

        // 1 验证令牌 必须是原子性的  脚本返回 0 1 成功1 不成功0
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        MemberTo memberTo = LoginUserInterceptor.toThreadLocal.get();
        //原子验证令牌和删除
        Long res = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberTo.getId()),
                orderToken
        );
        if (res == 1) {
            //  验证成功  下单 去创建订单 验证令牌 验证价格 锁库存

        } else {
            // 验证失败
            responseVo.setCode(1);
            return responseVo;
        }
        return responseVo;
    }

}