package com.zhyf.gulimall.product.feign;

import com.zhyf.common.to.es.SkuEsModel;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.product.feign.fallback.SearchFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "gulimall-search" , fallback = SearchFeignServiceFallback.class)
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
