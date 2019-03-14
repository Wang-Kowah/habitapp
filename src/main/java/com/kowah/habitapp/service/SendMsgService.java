package com.kowah.habitapp.service;

import com.kowah.habitapp.bean.enums.ErrorCode;
import com.kowah.habitapp.utils.EhCacheUtils;
import com.kowah.habitapp.utils.TencentSMSUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SendMsgService {
    private static final Logger logger = LoggerFactory.getLogger(SendMsgService.class);

    private final int CACHE_LIVE_SECONDS = 3 * 60;

    public ErrorCode sendVerifyCode(String mobile) {
        String code = generateVerificationCode();
        logger.debug("send verify code {}",code);

        ArrayList<String> params = new ArrayList<>();
        params.add(code);
        params.add("" + (CACHE_LIVE_SECONDS / 60));
        boolean flag = TencentSMSUtils.sendSMSTencent(params, mobile);
        if (!flag) {
            return ErrorCode.SEND_MSM_ERROR;
        }
        EhCacheUtils.put(getCacheKey(mobile), code, CACHE_LIVE_SECONDS);
        return ErrorCode.SUCCESS;
    }

    private String getCacheKey(String mobile) {
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
        String codeCache = (String) EhCacheUtils.get(getCacheKey(mobile));
        if (StringUtils.isEmpty(codeCache)) {
            return false;
        }

        return codeCache.equals(code);
    }
}
