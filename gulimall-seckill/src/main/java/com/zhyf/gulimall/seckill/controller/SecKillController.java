package com.zhyf.gulimall.seckill.controller;

import com.zhyf.common.utils.R;
import com.zhyf.gulimall.seckill.service.SeckillService;
import com.zhyf.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SecKillController {
    @Autowired
    SeckillService seckillService;

    /**
     * 当前时刻可以参与的秒杀服务
     *
     * @return R
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SecKillSkuRedisTo> skus = seckillService.getCurrentSeckillSkus();
        return R.ok().put("data", skus);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().put("data", to);
    }

    @GetMapping("/kill")
    public String kill(@RequestParam("killid") String killid,
                       @RequestParam("key") String key,
                       @RequestParam("num") Integer num,
                       Model model) {
        // islogin?
        String orderSn = seckillService.kill(killid, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
