package com.primaryhomework.backend.utils;

import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {

    private final Integer code;

    public CommonException(String message) {
        super(message);
        this.code = 1;
    }

    public CommonException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
