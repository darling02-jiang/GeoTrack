package com.geotrack.auth.dto;

import java.io.Serializable;

public class SessionUser implements Serializable {

    private Long userId;
    private String phone;
    private String nickname;

    public SessionUser() {
    }

    public SessionUser(Long userId, String phone, String nickname) {
        this.userId = userId;
        this.phone = phone;
        this.nickname = nickname;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
