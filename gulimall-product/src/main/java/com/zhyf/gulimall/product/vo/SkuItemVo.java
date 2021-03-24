package com.zhyf.gulimall.product.vo;

import com.zhyf.gulimall.product.entity.SkuImagesEntity;
import com.zhyf.gulimall.product.entity.SkuInfoEntity;
import com.zhyf.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    SkuInfoEntity info;
    List<SkuImagesEntity> images;
    List<SkuItemSaleAttrVo> saleAttr;
    SpuInfoDescEntity desp;
    List<SpuItemAttrGroupVo> groupAttrs;
    Boolean hasStock = true;
}
