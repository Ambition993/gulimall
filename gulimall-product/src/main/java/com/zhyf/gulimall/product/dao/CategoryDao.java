package com.zhyf.gulimall.product.dao;

import com.zhyf.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 12:04:21
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
