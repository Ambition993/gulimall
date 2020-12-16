package com.zhyf.gulimall.order.dao;

import com.zhyf.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 13:25:02
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
