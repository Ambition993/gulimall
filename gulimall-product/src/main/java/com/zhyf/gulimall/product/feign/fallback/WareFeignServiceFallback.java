package com.zhyf.gulimall.product.feign.fallback;

import com.zhyf.gulimall.product.feign.WareFeignService;
import com.zhyf.gulimall.product.vo.SkuHasStockVo;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class WareFeignServiceFallback implements WareFeignService {
    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        return null;
    }
}
