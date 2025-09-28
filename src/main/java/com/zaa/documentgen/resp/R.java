package com.zaa.documentgen.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class R<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;
    private Boolean success;

    public static <T> R<T> ok() {
        return R.<T>builder()
                .code(200)
                .message("success")
                .success(true)
                .build();
    }

    public static <T> R<T> fail() {
        return R.<T>builder()
                .code(-1)
                .message("fail")
                .success(false)
                .build();
    }

    public static <T> R<T> data(T data) {
        return R.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .success(true)
                .build();
    }



    public static <T> R<T> result(ResultCodeEnum codeEnum) {
        return R.<T>builder()
                .code(codeEnum.getCode())
                .message(codeEnum.getMessage())
                .success(false)
                .build();
    }

    public static <T> R<T> result(ResultCodeEnum codeEnum, String message) {
        return R.<T>builder()
                .code(codeEnum.getCode())
                .message(message)
                .success(false)
                .build();
    }
}
