package com.zhyf.gulimall.seckill.constant;

public class SecKillConstant {
    // 秒杀信息前缀
    public static final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    // 库存信息前缀
    public static final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    // 分布式信号量前缀
    public static final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; // + 随机码
    //分布式锁前缀
    public static final String SECKILL_UPLOAD_LOCK = "seckill:upload:lock";
}
