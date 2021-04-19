package com.zhyf.gulimall.product.feign.fallback;

import com.zhyf.common.exception.BizCodeEnum;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.product.feign.SecKillFeignService;
import org.springframework.stereotype.Component;

@Component
public class SecKillFeignServiceFallback implements SecKillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMessage());
    }
}
