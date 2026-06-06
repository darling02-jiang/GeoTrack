package com.geotrack.auth.support;

import com.geotrack.auth.dto.SessionUser;
import com.geotrack.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    public static final String SESSION_LOGIN_USER_KEY = "LOGIN_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BizException("未登录或会话已过期");
        }
        Object userObj = session.getAttribute(SESSION_LOGIN_USER_KEY);
        if (!(userObj instanceof SessionUser sessionUser)) {
            throw new BizException("未登录或会话已过期");
        }
        UserHolder.save(sessionUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.clear();
    }
}
