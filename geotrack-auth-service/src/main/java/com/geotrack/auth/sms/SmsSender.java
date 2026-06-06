package com.geotrack.auth.sms;

public interface SmsSender {

    void sendLoginCode(String phone, String code);
}
