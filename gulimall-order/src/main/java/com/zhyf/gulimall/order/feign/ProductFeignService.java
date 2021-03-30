package com.zhyf.gulimall.order.feign;

import com.zhyf.gulimall.order.vo.SpuInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/skuId/{id}")
    SpuInfoVo getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}
