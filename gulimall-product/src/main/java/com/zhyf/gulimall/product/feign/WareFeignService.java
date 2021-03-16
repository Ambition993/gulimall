package com.zhyf.gulimall.product.feign;

import com.zhyf.common.utils.R;
import com.zhyf.gulimall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
     R<List<SkuHasStockVo>> getSkusHasStock(@RequestBody List<Long> skuIds);
}
