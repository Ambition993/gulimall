package com.zhyf.gulimall.product.feign;

import com.zhyf.common.utils.R;
import com.zhyf.gulimall.product.feign.fallback.SecKillFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(value = "gulimall-seckill", fallback = SecKillFeignServiceFallback.class)
public interface SecKillFeignService {
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
