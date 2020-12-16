package com.zhyf.gulimall.member.dao;

import com.zhyf.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 13:23:59
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
