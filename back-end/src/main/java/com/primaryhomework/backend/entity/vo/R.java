package com.primaryhomework.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> R<T> ok(T data) {
        return new R<>(0, "success", data);
    }

    public static R<Void> ok() {
        return new R<>(0, "success", null);
    }

    public static <T> R<T> fail(String message) {
        return new R<>(1, message, null);
    }

    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null);
    }
}
