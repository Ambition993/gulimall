package com.zhyf.gulimall.order.web;

import com.zhyf.gulimall.order.service.OrderService;
import com.zhyf.gulimall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;
    @GetMapping("/toTrade")
    public String toTrade() {

       OrderConfirmVo confirmVo =  orderService.confirmOrder();
        return "confirm";
    }

}
