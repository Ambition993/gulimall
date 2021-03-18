package com.zhyf.gulimall.search.service;

import com.zhyf.gulimall.search.vo.SearchParam;
import com.zhyf.gulimall.search.vo.SearchResult;

public interface MallSearchService {
    /**
     * @param searchParam 页码传递过来的所有查询信息
     * @return 返回根据页面提供的信息查询到的结果
     */
    SearchResult search(SearchParam searchParam);
}
