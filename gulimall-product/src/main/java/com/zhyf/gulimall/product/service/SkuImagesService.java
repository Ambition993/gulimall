package com.zhyf.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.gulimall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 12:04:20
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuImagesEntity> getSkuImgsBySkuId(Long skuId);
}

