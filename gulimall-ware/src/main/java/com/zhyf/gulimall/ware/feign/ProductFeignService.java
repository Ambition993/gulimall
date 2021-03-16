package com.zhyf.gulimall.ware.feign;

import com.zhyf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping("/product/skuinfo/info/{skuId}")
//   @RequiresPermissions("product:skuinfo:info")
    R info(@PathVariable("skuId") Long skuId);
}
