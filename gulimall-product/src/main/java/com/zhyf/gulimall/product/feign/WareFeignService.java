package com.zhyf.gulimall.product.feign;

import com.zhyf.gulimall.product.feign.fallback.WareFeignServiceFallback;
import com.zhyf.gulimall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "gulimall-ware" , fallback = WareFeignServiceFallback.class)
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    List<SkuHasStockVo> getSkusHasStock(@RequestBody List<Long> skuIds);
}
