package com.zhyf.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zhyf.common.to.member.MemberTo;
import com.zhyf.common.to.mq.SeckillOrderTo;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.seckill.constant.SecKillConstant;
import com.zhyf.gulimall.seckill.feign.CouponFeignService;
import com.zhyf.gulimall.seckill.feign.ProductFeignService;
import com.zhyf.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.zhyf.gulimall.seckill.service.SeckillService;
import com.zhyf.gulimall.seckill.to.SecKillSkuRedisTo;
import com.zhyf.gulimall.seckill.vo.SeckillSessionsWithSkusVo;
import com.zhyf.gulimall.seckill.vo.SeckillSkuVo;
import com.zhyf.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 去数据库扫描需要参与秒杀的活动
        R session = couponFeignService.getLatest3DaysSession();
        if (session.getCode() == 0) {
            // 上架商品
            List<SeckillSessionsWithSkusVo> data = session.getData(new TypeReference<List<SeckillSessionsWithSkusVo>>() {
            });
            // 存到redis
            // 1 活动信息
            saveSessionInfos(data);
            // 2活动关联的商品信息
            saveSkuInfos(data);
        }
    }

    /**
     * 返回当前时间可以参与秒杀的商品
     *
     * @return
     */
    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        // 1 当前时间是哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SecKillConstant.SESSION_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SecKillConstant.SESSION_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            Long start = Long.parseLong(s[0]);
            Long end = Long.parseLong(s[1]);
            if (time >= start && time <= end) {
                // 2 这个秒杀场次有哪些商品
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(SecKillConstant.SKUKILL_CACHE_PREFIX);
                List<Object> list = hashOps.multiGet(range);
                if (list != null && list.size() > 0) {
                    List<SecKillSkuRedisTo> collect = list.stream().map(item -> {
                        SecKillSkuRedisTo redisTo = JSON.parseObject((String) item, SecKillSkuRedisTo.class);
                        // redisTo.setRandomCode(null); 当前秒杀开始了 可以返回随机码
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }


        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //找到所有要参与秒杀的key信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SecKillConstant.SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        String regx = "\\d_" + skuId;
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String toString = hashOps.get(key);
                    SecKillSkuRedisTo redisTo = JSON.parseObject(toString, SecKillSkuRedisTo.class);
                    //判断当前时间是不是秒杀时间 如果是就可以返回随机码
                    Long start = redisTo.getStartTime();
                    Long end = redisTo.getEndTime();
                    long current = new Date().getTime();
                    if (current >= start && current <= end) {
                        // 在秒杀时间内
                    } else {
                        // 没在秒杀时间内
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    /**
     * 秒杀方法
     *
     * @param killid
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killid, String key, Integer num) {
        // 获取登录用户的信息
        MemberTo memberTo = LoginUserInterceptor.toThreadLocal.get();

        // 秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps =
                redisTemplate.boundHashOps(SecKillConstant.SKUKILL_CACHE_PREFIX);
        String json = hashOps.get(killid);
        if (!StringUtils.isEmpty(json)) {
            SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
            // 1 时间合法性校验
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long time = new Date().getTime();
            if (time >= startTime && time <= endTime) {
                //2 随机码校验和商品id
                String skuIdAndSessionId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                String randomCode = redisTo.getRandomCode();
                if (randomCode.equals(key) && killid.equals(skuIdAndSessionId)) { // redis 里面的的 skuId和promotionsessionid 必须和请求的一样
                    // 3 验证购物数量是不是合理
                    Integer seckillLimit = redisTo.getSeckillLimit();
                    if (num <= seckillLimit) {
                        // 4 验证是否已经购买了过了 幂等性问题 只要秒杀成功了 就去redis里面站位置 userId_skuId_sessionId
                        String redisKey = memberTo.getId() + "_" + redisTo.getPromotionSessionId() + "_" + skuIdAndSessionId;
                        // SETNX
                        // 自动过期
                        long ttl = endTime - startTime;
                        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, String.valueOf(num), ttl, TimeUnit.MILLISECONDS);
                        if (ifAbsent) {
                            // 这个人没买过 可以购买 扣减信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SecKillConstant.SKU_STOCK_SEMAPHORE + randomCode);
                            //获取信号量
                            try {
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                // 秒杀成功了 快速下单 发送mq消息告知订单服务
                                String orderSn = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(orderSn);
                                orderTo.setMemberId(memberTo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                orderTo.setSkuId(redisTo.getSkuId());
                                orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                return orderSn;
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 在redis里面缓存活动信息
     *
     * @param sessions
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkusVo> sessions) {
        sessions.forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key = SecKillConstant.SESSION_CACHE_PREFIX + startTime + "_" + endTime;
            List<SeckillSkuVo> relationSkus = session.getRelationSkus();
            List<String> skuIds = relationSkus.stream().map(item -> item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
            ListOperations<String, String> opsForList = redisTemplate.opsForList();
            // 秒杀信息保存 seckill:sessions
            // 没上架的时候才上架保障了幂等性
            Boolean hasKey = redisTemplate.hasKey(key);
            if (hasKey != null && !hasKey) {
                opsForList.leftPushAll(key, skuIds);
            }
        });
    }


    /**
     * 在redis里面缓存活动关联的商品信息
     *
     * @param sessions
     */
    private void saveSkuInfos(List<SeckillSessionsWithSkusVo> sessions) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SecKillConstant.SKUKILL_CACHE_PREFIX);
        sessions.stream().forEach(session -> {
            session.getRelationSkus().forEach(seckillSkuVo -> {
                String token = UUID.randomUUID().toString().replace("_", "");
                Boolean key = ops.hasKey(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString()); // 当前场次+当前这个sku
                if (key != null && !key) { // 没有key才能保存 避免重复提交
                    SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                    // 1 sku基本信息
                    R res = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (res.getCode() == 0) {
                        SkuInfoVo skuInfo = res.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfoVo(skuInfo);
                    }
                    // 2 sku秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo, redisTo);
                    // 3 设置上当前商品的秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    // 4 设置随机码
                    redisTo.setRandomCode(token);
                    String s = JSON.toJSONString(redisTo);
                    // 5 商品sku详细信息保存 seckill:skus
                    ops.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString(), s); // 当前场次+当前这个sku

                    // 库存信息 如果当前这个场次的上商品的库存信息已经上架了不需要上架了
                    // 6 使用分布式信号来限制购买商品 seckill:stock
                    RSemaphore semaphore = redissonClient.getSemaphore(SecKillConstant.SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }
            });
        });
    }
}
