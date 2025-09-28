/**
 * com.genew.nuas.rest.swaggerfile
 * ModelAttr.java
 * 2022/4/11
 * Copyright (c) Genew Thchnologies 2010-2020. All rights reserved.
 */
package com.zaa.documentgen.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
public class ModelAttr implements Serializable
{

	/**
	 * 类名(只用于model的类名)
	 */
	private String className = "";
	/**
	 * 属性名(只用于model的属性)
	 */
	private String name = "";
	/**
	 * 类型
	 */
	private String type = "";
	/**
	 * 是否必填
	 */
	private Boolean require = false;
	/**
	 * 属性描述
	 */
	private String description;

	/**
	 * 基本数据类型： 默认值
	 */
	private Object defaultValue;
	/**
	 * 嵌套属性列表
	 */
	private List<ModelAttr> properties = new ArrayList<>();

	/**
	 * 是否加载完成，避免循环引用（用来标识model的，model的属性此字段不必在意）
	 */
	private boolean isCompleted = false;

}

