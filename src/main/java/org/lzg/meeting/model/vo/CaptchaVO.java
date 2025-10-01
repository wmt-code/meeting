package org.lzg.meeting.model.vo;

import lombok.Data;

@Data
public class CaptchaVO {
    private String captchaKey;
    private String captchaBase64;
}
