package com.zhyf.gulimall.search.controller;

import com.zhyf.gulimall.search.service.MallSearchService;
import com.zhyf.gulimall.search.vo.SearchParam;
import com.zhyf.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model) {
        // 根据页面来的查询参数去es中检索商品
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
