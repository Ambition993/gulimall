package com.zhyf.gulimall.seckill.service;

import com.zhyf.gulimall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

public interface SeckillService {
     void uploadSeckillSkuLatest3Days();

     List<SecKillSkuRedisTo> getCurrentSeckillSkus();

     SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killid, String key, Integer num);
}
