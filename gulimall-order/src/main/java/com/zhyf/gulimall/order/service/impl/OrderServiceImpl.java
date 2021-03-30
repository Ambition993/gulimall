package com.zhyf.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.to.member.MemberTo;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.gulimall.order.constant.OrderConstant;
import com.zhyf.gulimall.order.dao.OrderDao;
import com.zhyf.gulimall.order.entity.OrderEntity;
import com.zhyf.gulimall.order.entity.OrderItemEntity;
import com.zhyf.gulimall.order.feign.CartFeignService;
import com.zhyf.gulimall.order.feign.MemberFeignService;
import com.zhyf.gulimall.order.feign.ProductFeignService;
import com.zhyf.gulimall.order.feign.WmsFeignService;
import com.zhyf.gulimall.order.interceptor.LoginUserInterceptor;
import com.zhyf.gulimall.order.service.OrderService;
import com.zhyf.gulimall.order.service.to.OrderCreateTo;
import com.zhyf.gulimall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

    @Autowired
    ProductFeignService productFeignService;

    private final ThreadLocal<OrderSubmitVo> threadLocal = new ThreadLocal<>();

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
        threadLocal.set(vo);
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

    /**
     * 创建一个订单 并完成所有的流程
     *
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        // 1生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);
        // todo 收货时间
        // 2 获取到所有的订单项信息
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);
        // 3 计算价格
        computePrice(orderEntity, orderItems);
        return createTo;
    }

    /**
     * 计算价格  比对构建好的订单里面的价格和实际所有的订单项的价格是否相等
     *
     * @param orderEntity 订单
     * @param orderItems  订单里所有订单项
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems) {
        // 1 订单价格相关
        //
    }

    /**
     * 构建订单
     *
     * @param orderSn 订单号
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        // 1获取收货地址信息
        OrderSubmitVo submitVo = threadLocal.get();
        // 远程调用库存服务找到运费信息
        FareVo fareResp = wmsFeignService.getFare(submitVo.getAddrId());
        // 创建订单实体
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        // 运费信息
        orderEntity.setFreightAmount(fareResp.getFare());
        // 收货人信息
        orderEntity.setReceiverCity(fareResp.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareResp.getAddress().getName());
        orderEntity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        orderEntity.setReceiverPhone(fareResp.getAddress().getPhone());
        orderEntity.setReceiverProvince(fareResp.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareResp.getAddress().getRegion());
        return orderEntity;
    }

    /**
     * 构建所有订单项
     *
     * @param
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> orderItems = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItems;
        }
        return null;
    }

    /**
     * 构建一个订单项
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 1 订单信息 订单号 OK
        // 2 spu
        Long skuId = cartItem.getSkuId();
        SpuInfoVo spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        orderItemEntity.setSpuId(spuInfo.getId());
        orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfo.getSpuName());
        orderItemEntity.setCategoryId(spuInfo.getCatalogId());

        // 3 sku
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        // 4 优惠  不做
        // 5 积分
        orderItemEntity.setGiftGrowth(cartItem.getPrice().intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().intValue());
        return null;
    }
}