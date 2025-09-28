package com.zaa.documentgen.model;

import lombok.Data;

import java.io.Serializable;

/**
 * TODO Add class comment here<p/>
 *@version 1.0.0
 *@since 1.0.0
 *@author zhengaiao
 *@history<br/>
 * ver         date      author   desc
 * 1.0.0    2022/4/11      zhengaiao  created<br/>
 *<p/>
 */
@Data
public class Response implements Serializable{

    /**
     * 返回参数  ok
     */
    private String description;

    /**
     * 参数名  状态码: 200
     */
    private String name;

    /**
     * 备注 :R<String>
     */
    private String remark;

}
