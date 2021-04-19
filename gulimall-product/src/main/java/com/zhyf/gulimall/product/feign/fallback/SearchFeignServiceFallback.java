package com.zhyf.gulimall.product.feign.fallback;

import com.zhyf.common.exception.BizCodeEnum;
import com.zhyf.common.to.es.SkuEsModel;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.product.feign.SearchFeignService;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class SearchFeignServiceFallback  implements SearchFeignService {
    @Override
    public R productStatusUp(List<SkuEsModel> skuEsModels) {
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMessage());
    }
}
