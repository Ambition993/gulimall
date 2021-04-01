package com.zhyf.gulimall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MemberWebController {
    @RequestMapping("/memberOrder.html")
    public String memberOrderPage() {
        // 当前用户所有的订单的数据
        return "orderList";
    }
}
