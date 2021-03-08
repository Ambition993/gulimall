package com.zhyf.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo {
    //分类名字
    private String catelogName;
    //组名
    private String groupName;
    // categoryPath
    private Long[] categoryPath;
}
