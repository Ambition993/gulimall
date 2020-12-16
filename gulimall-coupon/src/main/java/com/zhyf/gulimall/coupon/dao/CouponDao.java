package com.zhyf.gulimall.coupon.dao;

import com.zhyf.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 13:21:46
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
