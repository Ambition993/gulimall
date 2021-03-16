package com.zhyf.gulimall.ware.dao;

import com.zhyf.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 13:25:56
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    long getSkuStock(@Param("skuId") Long skuId);
}
