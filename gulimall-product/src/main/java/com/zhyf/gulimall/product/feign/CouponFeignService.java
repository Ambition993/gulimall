package com.zhyf.gulimall.product.feign;

import com.zhyf.common.to.SkuReductionTo;
import com.zhyf.common.to.SpuBoundsTo;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.product.feign.fallback.CouponFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "gulimall-coupon", fallback = CouponFeignServiceFallback.class)
public interface CouponFeignService {
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo boundsTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSpuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
