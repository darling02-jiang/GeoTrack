package com.geotrack.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.auth.dto.LoginRequest;
import com.geotrack.auth.dto.LoginResponse;
import com.geotrack.auth.dto.SendCodeRequest;
import com.geotrack.auth.dto.SessionUser;
import com.geotrack.auth.entity.UserEntity;
import com.geotrack.auth.mapper.UserMapper;
import com.geotrack.auth.sms.SmsSender;
import com.geotrack.auth.support.LoginInterceptor;
import com.geotrack.auth.support.UserHolder;
import com.geotrack.common.exception.BizException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Random;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final String SESSION_LOGIN_CODE_KEY = "LOGIN_CODE";
    private static final String SESSION_LOGIN_CODE_PHONE_KEY = "LOGIN_CODE_PHONE";
    private static final String SESSION_LOGIN_CODE_TIME_KEY = "LOGIN_CODE_TIME";
    private static final String SESSION_LOGIN_CODE_LAST_SEND_TIME_KEY = "LOGIN_CODE_LAST_SEND_TIME";
    private static final long LOGIN_CODE_EXPIRE_MILLIS = 5 * 60 * 1000L;
    private static final long LOGIN_CODE_SEND_INTERVAL_MILLIS = 60 * 1000L;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final UserMapper userMapper;
    private final SmsSender smsSender;

    @Value("${sms.mock-enabled:true}")
    private boolean smsMockEnabled;

    @Value("${sms.mock-login-code:1234}")
    private String mockLoginCode;

    public AuthService(UserMapper userMapper, SmsSender smsSender) {
        this.userMapper = userMapper;
        this.smsSender = smsSender;
    }

    public String sendCode(SendCodeRequest request, HttpSession session) {
        if (request == null || !isPhoneValid(request.phone())) {
            throw new BizException("手机号格式不正确");
        }
        Long lastSendTime = (Long) session.getAttribute(SESSION_LOGIN_CODE_LAST_SEND_TIME_KEY);
        long now = System.currentTimeMillis();
        if (lastSendTime != null && now - lastSendTime < LOGIN_CODE_SEND_INTERVAL_MILLIS) {
            long remainSeconds = (LOGIN_CODE_SEND_INTERVAL_MILLIS - (now - lastSendTime) + 999) / 1000;
            throw new BizException("请求过于频繁，请" + remainSeconds + "秒后重试");
        }
        String code = smsMockEnabled ? mockLoginCode : generateCode();
        session.setAttribute(SESSION_LOGIN_CODE_KEY, code);
        session.setAttribute(SESSION_LOGIN_CODE_PHONE_KEY, request.phone());
        session.setAttribute(SESSION_LOGIN_CODE_TIME_KEY, now);
        session.setAttribute(SESSION_LOGIN_CODE_LAST_SEND_TIME_KEY, now);
        smsSender.sendLoginCode(request.phone(), code);
        return "验证码发送成功";
    }

    public LoginResponse login(LoginRequest request, HttpSession session) {
        if (request == null || !isPhoneValid(request.phone())) {
            throw new BizException("手机号不能为空");
        }
        String codeInSession = (String) session.getAttribute(SESSION_LOGIN_CODE_KEY);
        String phoneInSession = (String) session.getAttribute(SESSION_LOGIN_CODE_PHONE_KEY);
        Long codeTime = (Long) session.getAttribute(SESSION_LOGIN_CODE_TIME_KEY);
        if (!StringUtils.hasText(codeInSession) || !StringUtils.hasText(phoneInSession) || codeTime == null) {
            throw new BizException("验证码已过期，请重新获取");
        }
        long now = System.currentTimeMillis();
        if (now - codeTime > LOGIN_CODE_EXPIRE_MILLIS) {
            session.removeAttribute(SESSION_LOGIN_CODE_KEY);
            session.removeAttribute(SESSION_LOGIN_CODE_PHONE_KEY);
            session.removeAttribute(SESSION_LOGIN_CODE_TIME_KEY);
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!request.phone().equals(phoneInSession) || !codeInSession.equals(request.code())) {
            throw new BizException("验证码不正确");
        }
        session.removeAttribute(SESSION_LOGIN_CODE_KEY);
        session.removeAttribute(SESSION_LOGIN_CODE_PHONE_KEY);
        session.removeAttribute(SESSION_LOGIN_CODE_TIME_KEY);

        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getPhone, request.phone())
                .last("limit 1"));
        if (user == null) {
            user = new UserEntity();
            user.setPhone(request.phone());
            user.setPasswordHash(null);
            user.setNickname("用户" + request.phone().substring(Math.max(0, request.phone().length() - 4)));
            user.setPointsBalance(0);
            user.setStatus(1);
            userMapper.insert(user);
        }
        SessionUser sessionUser = new SessionUser(user.getId(), user.getPhone(), user.getNickname());
        session.setAttribute(LoginInterceptor.SESSION_LOGIN_USER_KEY, sessionUser);
        return new LoginResponse(user.getId(), user.getNickname(), user.getPhone());
    }

    public UserEntity currentUser() {
        SessionUser sessionUser = UserHolder.get();
        if (sessionUser == null || sessionUser.getUserId() == null) {
            throw new BizException("未登录");
        }
        Long userId = sessionUser.getUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    /** 不依赖拦截器与 UserHolder，避免 /me 匿名访问时误创建或污染会话 */
    public UserEntity findLoggedInUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object userObj = session.getAttribute(LoginInterceptor.SESSION_LOGIN_USER_KEY);
        if (!(userObj instanceof SessionUser sessionUser) || sessionUser.getUserId() == null) {
            return null;
        }
        return userMapper.selectById(sessionUser.getUserId());
    }

    public Long currentUserId() {
        SessionUser sessionUser = UserHolder.get();
        if (sessionUser == null || sessionUser.getUserId() == null) {
            throw new BizException("未登录");
        }
        return sessionUser.getUserId();
    }

    private boolean isPhoneValid(String phone) {
        return StringUtils.hasText(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    private String generateCode() {
        int value = 100000 + new Random().nextInt(900000);
        return String.valueOf(value);
    }
}
