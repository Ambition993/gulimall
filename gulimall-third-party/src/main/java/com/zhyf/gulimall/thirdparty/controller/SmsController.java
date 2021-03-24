package com.zhyf.gulimall.thirdparty.controller;

import com.zhyf.common.utils.R;
import com.zhyf.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SmsController {

    @Autowired
    SmsComponent smsComponent;

    @GetMapping("/sms/sendCode")
    public R sendSms(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsComponent.sendCode(phone, code);
        return R.ok();
    }
}
