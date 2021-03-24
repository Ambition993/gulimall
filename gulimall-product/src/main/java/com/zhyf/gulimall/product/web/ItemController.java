package com.zhyf.gulimall.product.web;

import com.zhyf.gulimall.product.service.SkuInfoService;
import com.zhyf.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {
    /**
     * 展示当前skuid商品的详情
     *
     * @param skuId
     * @return
     */
    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {
        System.out.println(skuId + "详情");
        SkuItemVo item = skuInfoService.item(skuId);
        model.addAttribute("item", item);
        return "item";
    }
}
