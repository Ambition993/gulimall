package com.zhyf.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.Map;

/**
 * 会员收货地址
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 13:23:59
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

