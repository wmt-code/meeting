package org.lzg.meeting.model.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 验证码key
     *
     */
    private String captchaKey;
    /**
     * 验证码
     */
    private String captchaCode;
}
