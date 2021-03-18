package com.zhyf.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递来的查询条件
 */
@Data
public class SearchParam {
    private String keyWord;
    private Long catagory3Id; // 三级分类的ID
    /**
     * sort=saleCount_asc
     * sort=skuPrice_asc
     * sort=hotScore_desc
     */
    private String sort; // 排序条件
    /**
     * 过滤条
     * hasStock=0/1
     * skuPrice=1_500 skuPrice=_500  skuPrice=500_
     * brandId=1
     * attrs=2_5寸:6寸
     */
    private Integer hasStock; // 是否有货
    private String skuPrice; // 商品价格
    private List<Long> brandId;  // 品牌ID 按照品牌进行查找
    private List<String> attrs; // 按照属性筛选
    private Integer pageNumber;
}
