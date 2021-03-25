package com.zhyf.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.gulimall.member.entity.MemberEntity;
import com.zhyf.gulimall.member.exception.PhoneExistException;
import com.zhyf.gulimall.member.exception.UserNameExistException;
import com.zhyf.gulimall.member.vo.MemberLoginVo;
import com.zhyf.gulimall.member.vo.MemberRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 13:23:59
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);
    void checkPhoneUnique(String phone) throws PhoneExistException;
    void checkUsernameUnique(String username)  throws UserNameExistException;

    MemberEntity login(MemberLoginVo loginVo);
}

