/**
 * com.genew.nuas.rest.web.util.swagger.util
 * AsposeWordUtil.java
 * 2022/5/26
 * Copyright (c) Genew Thchnologies 2010-2020. All rights reserved.
 */
package com.zaa.documentgen.gen;

import com.aspose.words.*;
import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.constants.InterfaceConst;
import com.zaa.documentgen.gen.directoryGen.DirectoryBuilder;
import com.zaa.documentgen.gen.homeGen.HomeIndexBuilder;
import com.zaa.documentgen.gen.pdfMark.PdfMarkBuilder;
import com.zaa.documentgen.gen.tableGen.ExampleTableBuilder;
import com.zaa.documentgen.gen.tableGen.ReqTableBuilder;
import com.zaa.documentgen.gen.tableGen.ResTableBuilder;
import com.zaa.documentgen.gen.textBuilder.TextBuilder;
import com.zaa.documentgen.gen.watermark.WaterMarkBuilder;
import com.zaa.documentgen.model.PdfBookModel;
import com.zaa.documentgen.util.TextUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * TODO Add class comment here
 * <p/>
 *
 * @version 1.0.0
 * @since 1.0.0
 * @author zhengaiao
 * @history<br/>
 *               ver date author desc
 *               1.0.0 2022/5/26 zhengaiao created<br/>
 *               <p/>
 */
@Slf4j
public class AsposeDocGenerator {
	/**
	 * 生成word文档-- 不包括 markdown api概述 的内容
	 *
	 * @param instance
	 * @param allInfo
	 * @param index
	 * @param desFile
	 */
	public static void generateSwaggerFile(DocxConf instance, Map<String, List<Map<String, Object>>> allInfo,
			Map<String, String> index, File desFile, String type) {
		log.info("======生成文档开始======");

		try {
			Document doc = new Document();
			DocumentBuilder builder = new DocumentBuilder(doc);

			/** 1.生成封面(首页) **/
			builder = HomeIndexBuilder.generateHomeIndex(instance, index, builder);
			log.info("generateSwaggerFile： 1.生成封面(首页) ");

			/** 2.插入目录占位标签 **/
			DirectoryBuilder.insertDirMark(builder, doc);
			log.info("generateSwaggerFile： 2.插入目录占位标签");

			/** 3.生成文档内容 **/
			generateSwaggerContent(instance, builder, allInfo);
			log.info("generateSwaggerFile： 3.生成文档内容");

			/** 4.生成页码 **/
			DirectoryBuilder.addHeaderFooter(doc);
			log.info("generateSwaggerFile： 4.生成页码");

			/** 5.生成文档目录 **/
			DirectoryBuilder.generateDirectory(doc);
			log.info("generateSwaggerFile： 5.生成文档目录");

			/** 6.生成水印* */
			String watermarkText = instance.getWatermarkText();
			if (!TextUtil.isBlank(watermarkText)) {
				WaterMarkBuilder.insertWatermarkText(doc, watermarkText);
				log.info("generateSwaggerFile： 6.生成水印");
			}

			if (type.equals("pdf")) {
				/** 7.word转pdf **/
				doc.save(desFile.getAbsolutePath(), SaveFormat.PDF);
				log.info("generateSwaggerFile： 7.保存为pdf");

				/** 8.pdf加标签 **/
				PdfMarkBuilder.pdfAddMark(doc, desFile);
				log.info("generateSwaggerFile： 8.pdf加标签");
				log.info("9.pdf加标签");
			} else if (type.equals("docx")) {
				doc.save(desFile.getAbsolutePath(), SaveFormat.DOCX);
				log.info("generateSwaggerFile： 7.保存为word");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("======生成文档结束======");

	}

	/**
	 * 二、生成文档内容
	 *
	 * @param doc
	 * @param allInfo
	 */

	public static void generateContent(DocxConf instance, Document doc,
			Map<String, List<Map<String, Object>>> allInfo) {

		DocumentBuilder builder = null;
		try {
			builder = new DocumentBuilder(doc);
			builder.moveToSection(1);
			generateSwaggerContent(instance, builder, allInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void generateSwaggerContent(DocxConf instance, DocumentBuilder builder,
			Map<String, List<Map<String, Object>>> allInfo) {
		Integer tagNum = 1;
		for (Map.Entry<String, List<Map<String, Object>>> entry : allInfo.entrySet()) {
			String key = entry.getKey();
			List<Map<String, Object>> list = entry.getValue();

			// 添加一级标题
			TextBuilder.addFirstTitle(builder, tagNum.toString(), key);
			for (int i = 0; i < list.size(); i++) {

				Map<String, Object> map = list.get(i);

				// 添加二级标题
				Double secondSeq = tagNum + (double) (i + 1) / 10;
				String subTitle = map.get(DocxConf.INTERFACE_NAME) == null ? ""
						: map.get(DocxConf.INTERFACE_NAME).toString();
				TextBuilder.addSecondTitle(builder, secondSeq.toString(), subTitle);
				// 添加接口基本信息 （接口地址/接口方式/请求类型/响应类型/接口描述）
				TextBuilder.addInterfaceContent(builder, map);

				// 请求参数（表格）
				TextBuilder.addThirdTitle(builder, "", InterfaceConst.interReq);
				Object o = map.get(DocxConf.INTERFACE_REQ) == null ? new ArrayList<>()
						: map.get(DocxConf.INTERFACE_REQ);
				List<Map<String, Object>> req = (List<Map<String, Object>>) o;
				if (req.size() > 0) {
					ReqTableBuilder.addReqParamTable(builder, req);
				} else {
					TextBuilder.addTextContent(builder, "无");
				}

				// 请求示例
				TextBuilder.addThirdTitle(builder, "", InterfaceConst.interReqExample);
				Map<String, String> reqExample = (Map<String, String>) map.get(DocxConf.INTERFACE_REQ_EXAMPLE);
				if (!reqExample.isEmpty()) {
					ExampleTableBuilder.addReqExample(builder, reqExample);
				} else {
					TextBuilder.addTextContent(builder, "无");
				}

				// 响应参数（表格）
				TextBuilder.addThirdTitle(builder, "", InterfaceConst.interRes);
				Object o1 = map.get(DocxConf.INTERFACE_RES) == null ? new ArrayList<>()
						: map.get(DocxConf.INTERFACE_RES);
				List<Map<String, Object>> res = (List<Map<String, Object>>) o1;
				if (res.size() > 0) {
					ResTableBuilder.addResParamTable(builder, res);
				} else {
					TextBuilder.addTextContent(builder, "无");
				}

				// 响应示例
				TextBuilder.addThirdTitle(builder, "", InterfaceConst.interResExample);
				Map<String, String> resExample = (Map<String, String>) map.get(DocxConf.INTERFACE_RES_EXAMPLE);
				if (!resExample.isEmpty()) {
					ExampleTableBuilder.addResExample(builder, resExample);
				} else {
					TextBuilder.addTextContent(builder, "无");
				}
				TextBuilder.addEmptyRow(builder, InterfaceConst.interfaceDescFontSize, 18.0d);
			}

			tagNum++;
		}
	}

	/**
	 * ====================================第四部分：
	 * 其他功能===============================================================
	 */

	/**
	 * 功能2： 获得所有标题，以便之后用来生成PDF的标签
	 *
	 * @param originDoc
	 * @return
	 */
	public static List<PdfBookModel> getAllTitle(Document originDoc) {

		// 用来存放所有标题，一级标题集合
		List<PdfBookModel> data = new ArrayList<>();
		try {

			// 后面需要用这个对象去获取当前段落所在的页码
			LayoutCollector layoutCollector = new LayoutCollector(originDoc);
			// 需要获取所有的section，要不然部分word提取目录不完整
			Section[] sections = originDoc.getSections().toArray();
			for (Section s : sections) {
				List<Paragraph> paragraphs = Arrays.asList(s.getBody().getParagraphs().toArray());

				for (Paragraph p : paragraphs) {
					/** 层级 **/
					int level = p.getParagraphFormat().getOutlineLevel();
					if (level < 1 || level > 2) {
						continue;
					}

					/** 页码 */
					int pageIndex = layoutCollector.getEndPageIndex(p);

					/** 标题内容 **/
					String text = "";
					for (Run run : p.getRuns()) {
						text += run.getText();
					}
					if (text == "") {
						continue;
					}

					/** 装填标签Model **/
					PdfBookModel model = new PdfBookModel();
					model.setLevel(level);
					model.setTitle(text);
					model.setPage(pageIndex);

					/** 装填标签Model **/
					if (level == 1) {
						data.add(model);
					} else {
						getChildTitle(data.get(data.size() - 1), model);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	private static void getChildTitle(PdfBookModel parent, PdfBookModel child) {
		if (parent.getLevel() + 1 == child.getLevel()) {
			parent.getChild().add(child);
		} else {
			getChildTitle(parent.getChild().get(parent.getChild().size() - 1), child);
		}
	}


	public static void docxToPdf(String docxPath, File pdfFile) {

		try {
			// 1. 加载 Word 文件
			Document doc = new Document(docxPath);

			// 2. 保存为 PDF
			doc.save(pdfFile.getAbsolutePath(), SaveFormat.PDF);
			log.info("Word 转 PDF 成功，输出路径: {}", pdfFile.getAbsolutePath());

			// 3. （可选）添加 PDF 标签
			PdfMarkBuilder.pdfAddMark(doc, pdfFile);

		} catch (Exception e) {
			log.error("转换失败", e);
			throw new RuntimeException("Word 转 PDF 异常", e);
		}
	}
}
