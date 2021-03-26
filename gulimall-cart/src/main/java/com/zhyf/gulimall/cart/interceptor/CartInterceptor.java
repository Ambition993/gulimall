package com.zhyf.gulimall.cart.interceptor;

import com.zhyf.common.constant.AuthServerConstant;
import com.zhyf.common.to.member.MemberTo;
import com.zhyf.gulimall.cart.To.UserInfoTo;
import com.zhyf.gulimall.cart.constant.CartConstant;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 执行目标方法之前 先来判断用户的登录状态 并封装传递给 controller 目标请求
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal;

    /**
     * 目标执行之前
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberTo member = (MemberTo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (member != null) {
            // 用户登录了
            userInfoTo.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                // user-key
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                }
            }
        }
        // 目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }
}
