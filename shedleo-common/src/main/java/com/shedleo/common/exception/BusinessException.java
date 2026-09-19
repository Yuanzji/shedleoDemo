package com.shedleo.common.exception;

import com.shedleo.common.result.ResultCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(@NotNull ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(@NotNull ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
