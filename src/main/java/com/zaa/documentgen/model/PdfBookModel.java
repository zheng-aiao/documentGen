package com.zaa.documentgen.model; /**
 * PACKAGE_NAME
 * PdfBookModel.java
 * 2022/5/24
 * Copyright (c) Genew Thchnologies 2010-2020. All rights reserved.
 */

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add class comment here<p/>
 *@version 1.0.0
 *@since 1.0.0
 *@author zhengaiao
 *@history<br/>
 * ver         date      author   desc
 * 1.0.0    2022/5/24      zhengaiao  created<br/>
 *<p/>
 */
@Getter
@Setter
public class PdfBookModel
{
	/** 标题层级 */
	public  int level;

	/** 标题文本 */
	public  String title;

	/** 所在页码 */
	public  int page;

    /**子标题*/
	public List<PdfBookModel> child;

	public PdfBookModel()
	{
		child = new ArrayList<>();
	}

	public PdfBookModel(int level, String title, int page, List<PdfBookModel> child)
	{
		this.level = level;
		this.title = title;
		this.page = page;
		this.child = child;
	}
}
