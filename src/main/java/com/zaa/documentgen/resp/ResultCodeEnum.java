package com.zaa.documentgen.resp;

public enum ResultCodeEnum {
    // http 请求代码
    SUCCESS(200, "success"),
    FAIL(-1, "Internal Server Error"),

    // 业务代码
    RESULT_FAIL_GET_SWAGGER_JSON(1000, "Obtain swagger json failed"),

    RESULT_FAIL_CREATE_FILE(2000, "create file failed");

    private final int code;
    private final String message;

    ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
