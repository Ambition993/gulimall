package com.zhyf.gulimall.order.web;

import com.zhyf.gulimall.order.service.OrderService;
import com.zhyf.gulimall.order.vo.OrderConfirmVo;
import com.zhyf.gulimall.order.vo.OrderSubmitVo;
import com.zhyf.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes attributes) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        // 去创建订单 令牌 价格 库存
        if (responseVo.getCode() == 0) {
            // 支付选择页
            model.addAttribute("submitOrderResp", responseVo);
            return "pay";
        } else {
            String msg = "下单失败：";
            switch (responseVo.getCode()) {
                case 1:
                    msg += "订单信息过期请刷新再提交";
                    break;
                case 2:
                    msg += "订单商品价格发生变化请确认后再次提交";
                    break;
                case 3:
                    msg += "库存锁定失败商品库存不足";
                    break;
            }
            // 下单失败回到订单确认订单信息
            attributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
