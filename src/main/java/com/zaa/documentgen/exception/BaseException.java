package com.zaa.documentgen.exception;

import com.zaa.documentgen.resp.ResultCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class BaseException extends RuntimeException
{
    private ResultCodeEnum resultCodeEnum;
    private String message;

    public BaseException(ResultCodeEnum resultCodeEnum, String desc)
    {
        super(desc);
        this.resultCodeEnum = resultCodeEnum;
        this.message = desc;
    }

    public BaseException(ResultCodeEnum resultCodeEnum)
    {
        super(resultCodeEnum.getMessage());
        this.resultCodeEnum = resultCodeEnum;
        this.message = resultCodeEnum.getMessage();
    }

}
