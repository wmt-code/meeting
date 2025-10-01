package org.lzg.meeting.common;

import lombok.Data;
import org.lzg.meeting.exception.ErrorCode;

import java.io.Serializable;

/**
 * 基础响应类
 *
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
