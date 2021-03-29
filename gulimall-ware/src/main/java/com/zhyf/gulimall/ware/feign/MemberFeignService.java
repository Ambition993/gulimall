package com.zhyf.gulimall.ware.feign;

import com.zhyf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @RequestMapping("/member/memberreceiveaddress/info/{id}")
//   @RequiresPermissions("product:memberreceiveaddress:info")
     R addrInfo(@PathVariable("id") Long id);
}
