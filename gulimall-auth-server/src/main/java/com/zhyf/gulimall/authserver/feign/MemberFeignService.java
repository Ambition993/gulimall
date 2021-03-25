package com.zhyf.gulimall.authserver.feign;

import com.zhyf.common.utils.R;
import com.zhyf.gulimall.authserver.vo.UserLoginVo;
import com.zhyf.gulimall.authserver.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo loginVo);
}
