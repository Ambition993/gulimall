package com.zhyf.gulimall.product.feign.fallback;

import com.zhyf.common.exception.BizCodeEnum;
import com.zhyf.common.to.SkuReductionTo;
import com.zhyf.common.to.SpuBoundsTo;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.product.feign.CouponFeignService;
import org.springframework.stereotype.Component;

@Component
public class CouponFeignServiceFallback implements CouponFeignService {
    @Override
    public R saveSpuBounds(SpuBoundsTo boundsTo) {
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMessage());
    }

    @Override
    public R saveSpuReduction(SkuReductionTo skuReductionTo) {
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMessage());
    }
}
