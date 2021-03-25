package com.zhyf.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.gulimall.member.dao.MemberDao;
import com.zhyf.gulimall.member.dao.MemberLevelDao;
import com.zhyf.gulimall.member.entity.MemberEntity;
import com.zhyf.gulimall.member.entity.MemberLevelEntity;
import com.zhyf.gulimall.member.exception.PhoneExistException;
import com.zhyf.gulimall.member.exception.UserNameExistException;
import com.zhyf.gulimall.member.service.MemberService;
import com.zhyf.gulimall.member.vo.MemberLoginVo;
import com.zhyf.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 用户注册
     *
     * @param vo
     */
    @Override
    public void regist(MemberRegistVo vo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity memberLevel = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevel.getId());

        // 检查用户名和手机是否唯一 为了让controller感知异常 使用异常机制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        // 密码设置 进行加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encodedPassword);
        memberEntity.setNickname(UUID.randomUUID().toString().substring(0, 6));
        memberDao.insert(memberEntity);
    }

    /**
     * 用户手机号查重
     *
     * @param phone
     * @throws PhoneExistException
     */
    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    /**
     * 用户名查重
     *
     * @param username
     * @throws UserNameExistException
     */
    @Override
    public void checkUsernameUnique(String username) throws UserNameExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    /**
     * 用户登录功能  密码校验
     *
     * @param loginVo
     * @return
     */
    @Override
    public MemberEntity login(MemberLoginVo loginVo) {
        String loginAccount = loginVo.getLoginAccount();
        String password = loginVo.getPassword();

        // 去数据库查询
        MemberDao baseMapper = this.baseMapper;
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAccount).or().eq("mobile", loginAccount));
        if (memberEntity == null) {
            return null;
        } else {
            // 获取到数据里面的password
            String passwordInDb = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            // 密码比对
            boolean matches = passwordEncoder.matches(password, passwordInDb);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

}