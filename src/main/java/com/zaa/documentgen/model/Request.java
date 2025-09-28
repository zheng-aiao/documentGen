/**
 * com.genew.nuas.rest.swaggerfile
 * Request.java
 * 2022/4/11
 * Copyright (c) Genew Thchnologies 2010-2020. All rights reserved.
 */
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
public class Request implements Serializable{


    /**
     * 参数名称
     */
    private String name;

    /**
     * 数据类型
     */
    private String type;

    /**
     * 请求类型 对应in
     */
    private String paramType;

    /**
     * 是否必填
     */
    private String require;

    /**
     * 参数说明
     */
    private String description;
    /**
     * 默认值
     */
    private Object defaultValue;

    /**
     * 复杂对象引用
     */
    private ModelAttr modelAttr;

}
