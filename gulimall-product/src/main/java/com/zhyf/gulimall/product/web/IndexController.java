package com.zhyf.gulimall.product.web;

import com.zhyf.gulimall.product.entity.CategoryEntity;
import com.zhyf.gulimall.product.service.CategoryService;
import com.zhyf.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        // 查询所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @GetMapping("/index/catelog.json")
    @ResponseBody
    public Map<String,  List<Catelog2Vo>> getCatelogJson() {
        return categoryService.getCatelogJson();
    }
}
