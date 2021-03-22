package com.zhyf.gulimall.search.feign;

import com.zhyf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @GetMapping("/producta/attr/info/{attrId}")
//   @RequiresPermissions("product:attr:info")
    R attrInfo(@PathVariable("attrId") Long attrId);
}
