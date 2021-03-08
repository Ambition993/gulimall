package com.zhyf.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.gulimall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 12:04:21
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}
