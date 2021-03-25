package com.zhyf.gulimall.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.zhyf.common.constant.AuthServerConstant;
import com.zhyf.common.exception.BizCodeEnum;
import com.zhyf.common.to.member.MemberTo;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.authserver.feign.MemberFeignService;
import com.zhyf.gulimall.authserver.feign.ThirdPartFeignService;
import com.zhyf.gulimall.authserver.vo.UserLoginVo;
import com.zhyf.gulimall.authserver.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//
@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 接口防刷
        String redisCode = ops.get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time < 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        String s = UUID.randomUUID().toString();
        String code = s.substring(0, 5);
        // 验证码再次校验 sms:code:19950374946 -> 123456
        // 缓存验证码 用于校验
        ops.set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code + "_" + System.currentTimeMillis(), 10, TimeUnit.MINUTES);
        thirdPartFeignService.sendSms(phone, code);
        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo registVo, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            Map<String, String> errors;
            errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors", errors);
            // 错误后返回页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        // 真正注册 远程服务
        // 1 验证码
        String code = registVo.getCode();
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String redisCode = ops.get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registVo.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {
            if (code.equals(redisCode.split("_")[0])) {
                // 验证码通过
                // 删除验证码
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registVo.getPhone());
                // 远程调用
                R r = memberFeignService.regist(registVo);
                if (r.getCode() == 0) {
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getMsg());
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                HashMap<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            HashMap<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo loginVo, RedirectAttributes redirectAttributes, HttpSession session) {
        // 远程登录
        R r = memberFeignService.login(loginVo);
        if (r.getCode() == 0) {
            // 成功
            MemberTo data = r.getData("data", new TypeReference<MemberTo>() {
            });

            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getMsg());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            return "login";
        } else {
            return "redirect:http://gulimall.com";
        }
    }
}

