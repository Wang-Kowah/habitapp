package com.kowah.habitapp.bean.enums;

public enum ErrorCode {

    SUCCESS(0, "成功"),

    // 系统相关错误码1000
    PARAM_ERROR(1001, "参数错误"),
    TIME_OUT(1002, "请求超时"),
    SYSTEM_ERROR(1003, "系统错误，请稍后再试！"),
    UNKNOWN_ERROR(1004, "未知错误，请稍后再试！"),

    // 登录相关
    LOGIN_FAIL(2001, "登录失败,用户名或者密码错误!"),
    LOGINOUT_ERROR(2003, "退出登录失败!"),

    // 手机验证码相关错误码4000
    MOBILE_EXIST_ERROR(4001, "该手机号码已经注册"),
    SEND_MSM_ERROR(4002, "短信发送失败"),
    MSM_CODE_ERROR(4003, "短信验证码不正确"),
    MOBILE_NOT_EXIST_ERROR(4004, "该手机号码不存在"),

    // 用户相关错误码6000
    USER_IS_NOT_EXIST(6001, "用户不存在"),
    UPLOAD_PROFILE_PIC_ERROR(6002,"上传头像失败，请稍后再试！");

    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
