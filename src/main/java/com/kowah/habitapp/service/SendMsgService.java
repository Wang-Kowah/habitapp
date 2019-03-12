package com.kowah.habitapp.service;

import com.kowah.habitapp.bean.enums.ErrorCode;
import com.kowah.habitapp.utils.EhCacheUtils;
import com.kowah.habitapp.utils.TencentSMSUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SendMsgService {

    private final int CACHE_LIVE_SECONDS = 10 * 60;

    public ErrorCode sendVerifyCode(String mobile) {
        String code = generateVerificationCode();
        ArrayList<String> params = new ArrayList<>();
        params.add(code);
        params.add("" + (CACHE_LIVE_SECONDS / 60));
        boolean flag = TencentSMSUtils.sendSMSTencent(params, mobile);
        if (!flag) {
            return ErrorCode.SEND_MSM_ERROR;
        }
        EhCacheUtils.put(getCachekey(mobile), code, CACHE_LIVE_SECONDS);
        return ErrorCode.SUCCESS;
    }

    private String getCachekey(String mobile) {
        return "mobile_code_" + mobile;
    }

    /**
     * 生成4位验证码
     */
    private String generateVerificationCode() {
        return RandomStringUtils.random(4, "0123456789");
    }

    /**
     * 校验用户输入的验证码
     */
    public boolean checkCode(String mobile, String code) {
        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(code)) {
            return false;
        }
        String codeCache = (String) EhCacheUtils.get(getCachekey(mobile));
        if (StringUtils.isEmpty(codeCache)) {
            return false;
        }

        return codeCache.equals(code);
    }
}
