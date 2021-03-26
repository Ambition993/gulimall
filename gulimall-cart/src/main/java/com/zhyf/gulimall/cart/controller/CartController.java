package com.zhyf.gulimall.cart.controller;

import com.zhyf.gulimall.cart.interceptor.CartInterceptor;
import com.zhyf.gulimall.cart.service.CartService;
import com.zhyf.gulimall.cart.to.UserInfoTo;
import com.zhyf.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

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

    /**
     * 添加到购物车
     * attributes.addFlashAttribute() 把数据放进session中 可以在页面取出 只能用一次
     * attributes.addAttribute("skuId", skuId); 把数据拼接在url后面
     *
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes attributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        attributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 跳转到成功页面  上面的方法重定向之前附加了skuID 重新查一次购物项即可 避免了重复提交
     *
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId,
                                   Model model) {
        //重定向到成功页面 再查一下购物车数据即可
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item", item);
        return "success";
    }
}
