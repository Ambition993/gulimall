package com.zhyf.gulimall.member.openfeign;

import com.zhyf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;


@FeignClient("gulimall-coupon-service")
public interface CouponFeignService {

    @RequestMapping("coupon/coupon/member/list")
    R membercoupon();
}
