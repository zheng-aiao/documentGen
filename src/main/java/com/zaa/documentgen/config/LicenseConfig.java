/**
 * com.genew.nuas.rest.web.util.swagger.util
 * SwaggerConfig.java
 * 2022/4/24
 * Copyright (c) Genew Thchnologies 2010-2020. All rights reserved.
 */
package com.zaa.documentgen.config;

import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.aspose.words.FontSettings;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * TODO Add class comment here
 * <p/>
 *
 * @version 1.0.0
 * @since 1.0.0
 * @author zhengaiao
 * @history<br/>
 *               ver date author desc
 *               1.0.0 2022/4/24 zhengaiao created<br/>
 *               <p/>
 */
@Slf4j
public class LicenseConfig {

	public static void setConfig() {
		log.info("load license information for apache words and pdf and Set Chinese font");
		/** 1、读入依赖的Apache.poi的许可证信息 **/
		LicenseConfig.getWordLicense();
		LicenseConfig.getPdfLicense();

		/** 2、设置中文字体 **/
		String path = LicenseConfig.class.getClassLoader().getResource("chinese-font").getPath();
		OsInfo osInfo = SystemUtil.getOsInfo();
		log.info("The current system operating system is:" + osInfo.getName());
		if (osInfo.isLinux()) {
			FontSettings.setFontsFolder("/usr/local/nuas-rest/conf/chinese-font", true);
			log.info("The path of the project font folder in the current server is:"
					+ "/usr/local/nuas-rest/conf/chinese-font");
		} else {
			FontSettings.setFontsFolder(path, true);
			log.info("The path of the project font folder in the current server is:" + path);
		}
	}

	public static void getWordLicense() {

		try {
			InputStream is = LicenseConfig.class.getClassLoader().getResourceAsStream("license.xml");
			final com.aspose.words.License license = new com.aspose.words.License();
			license.setLicense(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getPdfLicense() {

		try {
			final com.aspose.pdf.License license = new com.aspose.pdf.License();
			InputStream is = LicenseConfig.class.getClassLoader().getResourceAsStream("license.xml");
			license.setLicense(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
