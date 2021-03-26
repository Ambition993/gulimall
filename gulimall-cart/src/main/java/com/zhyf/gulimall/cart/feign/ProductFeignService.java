package com.zhyf.gulimall.cart.feign;

import com.zhyf.common.utils.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

public interface ProductFeignService {
    @RequestMapping("/producnt/skuinfo/info/{skuId}")
//   @RequiresPermissions("product:skuinfo:info")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);
}
