package com.zhyf.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.gulimall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 12:04:21
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);

    List<BrandEntity> getBrandsByIds(List<Long> brandIds);
}

