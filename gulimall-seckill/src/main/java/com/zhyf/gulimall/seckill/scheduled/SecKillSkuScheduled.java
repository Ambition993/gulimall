package com.zhyf.gulimall.seckill.scheduled;

import com.zhyf.gulimall.seckill.constant.SecKillConstant;
import com.zhyf.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品定时上架
 * 每晚3点上架最新三天需要秒杀的商品
 * 当天 00：00-23：59
 * 明天 00：00-23：59
 * 后天 00：00-23：59
 */
@Slf4j
@Service
public class SecKillSkuScheduled {
    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        // 分布式锁来保证多线程执行上架时候，只有一个线程可以访问该方法 释放锁后其他人可以获取到新的状态  在方法内部再处理幂等性问题
        log.info("upload");
        RLock lock = redissonClient.getLock(SecKillConstant.SECKILL_UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }
}
