package com.kowah.habitapp.utils;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class TencentSMSUtils {
    private static final Logger logger = LoggerFactory.getLogger(TencentSMSUtils.class);

    private static final int APPID = 1400191462;
    private static final String APPKEY = "94cdb9a0598a0079b6fa518fed24bc64";

    /**
     * 腾讯云短信模板id
     */
    // 欢迎使用习惯APP，您此次的操作验证码是{1},请于{2}分钟内填写。如非本人操作,请忽略本短信。
    private static final int TENCENT_SMS_VERIFY_CODE_TEMPLATE_ID = 292986;
    // 短信签名
    private static final String TENCENT_SMS_SIGN = "习惯APP";

    /**
     * 发送验证码
     */
    public static boolean sendSMSTencent(ArrayList<String> params, String mobile) {
        try {
            logger.debug("Send msg success, mobile={},verify code={}", mobile, params.get(0));
            // 数组具体的元素个数和模板中变量个数必须一致，例如事例中templateId:5678对应一个变量，参数数组中元素个数也必须是一个
            SmsSingleSender ssender = new SmsSingleSender(APPID, APPKEY);
            // 签名参数未提供或者为空时，会使用默认签名发送短信
            SmsSingleSenderResult result = ssender.sendWithParam("86", mobile, TENCENT_SMS_VERIFY_CODE_TEMPLATE_ID, params, TENCENT_SMS_SIGN, "", "");
            if (result != null && result.result == 0) {
                return true;
            } else {
                logger.error("Send msg fail", result);
            }
        } catch (HTTPException | IOException e) {
            logger.error("Send msg fail", e);
        }
        return false;
    }
}
