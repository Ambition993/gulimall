package com.zhyf.gulimall.order.interceptor;

import com.zhyf.common.constant.AuthServerConstant;
import com.zhyf.common.to.member.MemberTo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberTo> toThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberTo attribute = (MemberTo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null) {
            toThreadLocal.set(attribute);
            return true;
        } else {
            // 没登录先去登录
            session.setAttribute("msg", "请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
