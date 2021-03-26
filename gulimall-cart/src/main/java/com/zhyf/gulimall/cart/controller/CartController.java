package com.zhyf.gulimall.cart.controller;

import com.zhyf.gulimall.cart.To.UserInfoTo;
import com.zhyf.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class CartController {
    /**
     * 浏览器有一个cookie user-key 用来标识用户身份 一个月过期
     * 第一次使用 将会给一个临时的用户身份
     * 浏览器以后保存 每次访问都会带上
     * 登录了 session里面有  没登录cookie里面user-key
     * 第一次 如果没临时用户 就创建一个临时用户
     * 用拦截器来处理登录信
     *
     * @param session
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session) {
        // 快速获得信息 利用threadLocal
        // 通过threadLocal 取得userinfoTo
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        System.out.println(userInfoTo);
        return "cartList";
    }

}
