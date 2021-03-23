package com.zhyf.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhyf.gulimall.product.entity.AttrGroupEntity;
import com.zhyf.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 12:04:21
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
