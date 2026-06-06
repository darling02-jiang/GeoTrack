package com.geotrack.auth.sms;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.geotrack.common.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AliyunSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsSender.class);

    @Value("${sms.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${sms.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${sms.aliyun.sign-name:}")
    private String signName;

    @Value("${sms.aliyun.template-code:}")
    private String templateCode;

    @Value("${sms.aliyun.region-id:cn-hangzhou}")
    private String regionId;

    @Value("${sms.mock-enabled:true}")
    private boolean mockEnabled;

    @Override
    public void sendLoginCode(String phone, String code) {
        if (mockEnabled) {
            log.info("SMS_MOCK phone={}, code={}", phone, code);
            return;
        }
        if (!StringUtils.hasText(accessKeyId)
                || !StringUtils.hasText(accessKeySecret)
                || !StringUtils.hasText(signName)
                || !StringUtils.hasText(templateCode)) {
            throw new BizException("短信服务配置不完整");
        }

        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", regionId);
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");

        try {
            CommonResponse response = client.getCommonResponse(request);
            if (response == null || !StringUtils.hasText(response.getData()) || !response.getData().contains("\"Code\":\"OK\"")) {
                throw new BizException("短信发送失败");
            }
        } catch (ClientException e) {
            throw new BizException("短信发送异常: " + e.getErrMsg());
        }
    }
}
