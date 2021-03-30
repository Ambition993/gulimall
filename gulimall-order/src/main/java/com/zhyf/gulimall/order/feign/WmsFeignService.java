package com.zhyf.gulimall.order.feign;

import com.zhyf.common.utils.R;
import com.zhyf.gulimall.order.vo.FareVo;
import com.zhyf.gulimall.order.vo.SkuStockVo;
import com.zhyf.gulimall.order.vo.WareSkuLockVo;
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

    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo wareSkuLockVo);

}
