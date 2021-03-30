package com.zhyf.gulimall.order.feign;

import com.zhyf.gulimall.order.vo.FareVo;
import com.zhyf.gulimall.order.vo.SkuStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WmsFeignService {
    @PostMapping("/ware/waresku/hasstock")
    List<SkuStockVo> getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    @ResponseBody
    FareVo getFare(@RequestParam("addrId") Long addrId);
}
