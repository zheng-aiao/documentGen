//package com.zaa.documentgen.util;
//
//import cn.hutool.system.OsInfo;
//import cn.hutool.system.SystemUtil;
//import com.aspose.words.*;
//import com.genew.nuas.cube.common.entity.R;
//import com.genew.nuas.cube.common.entity.ResultCodeEnum;
//import com.genew.nuas.cube.common.utils.DateUtils;
//import com.genew.nuas.cube.common.utils.FileUtils;
//import com.genew.nuas.cube.common.utils.StringUtils;
//import com.genew.nuas.rest.web.util.swagger.model.PdfBookModel;
//import com.genew.nuas.rest.web.util.swagger.model.SetDocxConf;
//import lombok.extern.slf4j.Slf4j;
//
//import java.awt.*;
//import java.io.*;
//import java.util.List;
//import java.util.*;
//
//;
//
//@Slf4j
//public class MdToWordUtil
//{
//
//	public static String PDF = "pdf";
//	public static String WORD = "docx";
//
//
//	/**
//	 * 配置类
//	 */
//	public static SetDocxConf docxConf = SetDocxConf.getInstance();
//
//	/**
//	 * 逻辑入口
//	 *
//	 * @param format     生成文件的格式
//	 * @param sourcePath 指定md存放的目录路径
//	 * @return
//	 */
//	public static List<File> mdToWordMain(String format, String sourcePath , Boolean isIncludeSubDir ,Boolean isMerge)
//	{
//
//		/** 1. 调用python 将目录下的每一个md生成对应的word */
//
//		String descDir = docxConf.getMdToWordFilePath();
//		FileUtils.createDir(descDir);
//		String sourDir = sourcePath;;
//		log.info(sourDir);
//		log.info(descDir);
//
//		/** md -> word 异步线程要控制， 先生成对应文件在考虑合成一个**/
//
//		mdFileToWordFile(sourDir, descDir ,isIncludeSubDir ,isMerge);
//
//		/** 2. 获得存放python生成好的word文档的的路径 */
//		List<File> fileList = new ArrayList<>();
//		//从根目录获得下边各个实际存放md文件的目录
//		List<String> dirs = FileUtils.getAllFileName(descDir , true);
//		for (String dir : dirs)
//		{
//			log.info("process merged directory : " + dir);
//			/** 根据指定格式，生成对应文件名**/
//			String[] path = null ;
//
//			OsInfo osInfo = SystemUtil.getOsInfo();
//			if (osInfo.isWindows()){
//				path = dir.split("\\\\");
//			}else if(osInfo.isLinux()){
//				path = dir.split("/");
//			}else{
//				//预留
//				log.info("This interface currently only supports window and linux servers running");
//			}
//
//			String type = path[path.length - 1];
//			String desc_file_name = "";
//			if (format.equals(WORD))
//			{
//				desc_file_name = type + ".docx";
//			}
//			if (format.equals(PDF))
//			{
//				desc_file_name = type + ".pdf";
//			}
//
//			File desc_file = new File(docxConf.getMdMergeFilePath(), desc_file_name);
//			FileUtils.createFile(desc_file);
//			/** 合并多个word文档**/
//			mergeDocument(dir, desc_file, format ,"docx" ,true);
//			fileList.add(desc_file);
//		}
//		/** 删除md转为word的单个文件，避免新增或减少md，但合并时仍老路径下仍有原docx文件，导致多余合并**/
//		FileUtils.deleteAllFile(descDir);
//		return fileList;
//
//	}
//
//
//
//	/**
//	 * 将下级每一个md转换为对应的word,同级文件夹下输出，可指定文件夹
//	 * @param format 格式
//	 * @param sourcePath 源路径
//	 * @return
//	 */
//	public static void mdToWordByOne(String format, String sourcePath, Boolean isIncludeSubDir ,Boolean isMerge ){
//		mdFileToWordFile(sourcePath,sourcePath,isIncludeSubDir, isMerge);
//		mergeDocument(sourcePath, null, format, "docx", isMerge);
//
//	}
//
//	/**---------------------主要逻辑-----------------------------------------------------------------------------------------------------*/
//
//
//	/**
//	 * 生成 并执行 pandoc 命令， 生成对应md文件的word
//	 * 在合并时，descDir 有效，生成所有单个word，均保存在该路径下
//	 * 在不合并时，descDir 无效， 生成的单个word均在对应的md的同级目录下
//	 *
//	 * @param sourceDir 要处理的目录 （存放md）
//	 * @param descDir   存放转换成word的目录 （存放md-》word,一一对应）
//	 * @param isIncludeSubDir   true 表示含所有下级 ， false表只处理本目录
//	 * @param isMerge   true 表示md文件按同一目录下合并 ， false表 不合并，同级产生要转换的文件
//	 */
//	public static void mdFileToWordFile(String sourceDir, String descDir ,Boolean isIncludeSubDir ,Boolean isMerge)
//	{
//		/** 根据 sourceDir 找到它下边所有md结文件的绝对路径 */
//		List<File> mdList = FileUtils.getFileListByExt(sourceDir, isIncludeSubDir, "md");
//		/** 根据原md绝对路径 生成 word的目的路径  */
//		for (File file : mdList)
//		{
//			OsInfo osInfo = SystemUtil.getOsInfo();
//			String[] pathArray = null;
//			if (osInfo.isWindows()){
//				pathArray = file.getAbsolutePath().split("\\\\");
//			}else if(osInfo.isLinux()){
//				pathArray = file.getAbsolutePath().split("/");
//			}else{
//				//预留
//				log.info("This interface currently only supports window and linux servers running");
//			}
//			String descFilePath = "";
//			if(isMerge){
//				String descDirPath = new File(descDir + "/" +pathArray[pathArray.length-2]).getAbsolutePath();
//				FileUtils.createDir(descDirPath);
//				String name = file.getName().replace("md", "docx");
//				descFilePath = descDirPath+ "/" + name;
//			}else{
//				descFilePath = file.getAbsolutePath().replace("md", "docx");
//			}
//			exePandocCmd(file.getAbsolutePath(), descFilePath);
//		}
//	}
//
//	public static void exePandocCmd(String sourPath, String descPath)
//	{
//		/**  生成调用pandoc的转换命令， 并执行 */
//		String pandocCmd = "pandoc -s " + sourPath + " -o " + descPath;
//		log.info( pandocCmd);
//		try
//		{
//			Process pr = Runtime.getRuntime().exec(pandocCmd);
//			BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//			String line;
//			while ((line = in.readLine()) != null)
//			{
//				log.info(line);
//			}
//			in.close();
//			pr.waitFor();
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * @param sour_path 要合并的word所在的目录
//	 * @param desc_file 生成后的word所在目录
//	 * @param format 最后要生成的格式
//	 * @param ext 指定合并文件类型
//	 * @param isMerge  sour_path下的文件是否合并
//	 * @Description: 合并指定文件夹下的所有word文档，并建立索引目录
//	 */
//	public static void mergeDocument(String sour_path, File desc_file, String format ,String ext ,Boolean isMerge )
//	{
//
//		String[] sour_absolute_path = FileUtils.getFileAllPathByExt(sour_path, true ,ext);
//		try
//		{
//			if(isMerge){
//				List<Document> documents = new ArrayList<>();
//				/** 要合并的各个 api概述 word文档**/
//				for (String fileName : sour_absolute_path)
//				{
//					documents.add(new Document(fileName));
//				}
//				/** 只有api概述 **/
//				generateDocx(documents , desc_file ,format);
//			}else{
//				for (String fileName : sour_absolute_path)
//				{
//					List<Document> documents = new ArrayList<>();
//					documents.add(new Document(fileName));
//
//					File file = new File(fileName);
//					String absolutePath = file.getAbsolutePath();
//					String prefix ="";
//					File newFile = null;
//					OsInfo osInfo = SystemUtil.getOsInfo();
//					if (osInfo.isWindows()){
//						String[] split = absolutePath.split("\\\\");
//						prefix = split[split.length - 2];
//						absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("\\"));
//						newFile = new File(absolutePath+"\\"+prefix , file.getName().replace("docx", format));
//					}
//					if(osInfo.isLinux()){
//						String[] split = absolutePath.split("/");
//						prefix = split[split.length - 2];
//						absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
//						newFile = new File(absolutePath+"/"+prefix , file.getName().replace("docx", format));
//					}
//					FileUtils.createFile(newFile);
//					generateDocx(documents ,newFile ,format);
//					file.delete();
//				}
//			}
//
//		} catch (Exception e)
//		{
//			R.result(ResultCodeEnum.RESULT_MERGE_WORD_FILE, "Failed to merge multiple words, Consider the asynchronous process problem");
//		}
//	}
//
//	/**
//	 * 生成文档
//	 *
//	 * @param documents 原文档可以多个
//	 * @param desc_file 目的文件名
//	 * @param format 输出格式
//	 */
//	public static void generateDocx(List<Document> documents, File desc_file, String format){
//		FileOutputStream os = null;
//		try
//		{
//			Document doc = new Document();
//			os = new FileOutputStream(desc_file);
//
//			/** 生成封面 */
//			Map<String, String> index = new HashMap<>();
//			index.put(SetDocxConf.INDEX_TIME, DateUtils.now(null));
//			index.put(SetDocxConf.INDEX_EMAIL, docxConf.getEmail());
//			index.put(SetDocxConf.INDEX_URL, docxConf.getUrl());
//			index.put(SetDocxConf.INDEX_VERSIONDOCX, docxConf.getUrl());
//			DocumentBuilder builder = generateCover(doc, index);
//
//			/** 追加其他文档**/
//			for (Document document : documents)
//			{
//				doc.appendDocument(document, ImportFormatMode.USE_DESTINATION_STYLES);
//			}
//			/** 添加页码 **/
//			addHeaderFooter(doc);
//			/** 添加目录 **/
//			generateDirectory(doc);
//			/** 删除文档现有的所以书签*/
//			BookmarkCollection bookmarks = doc.getRange().getBookmarks();
//			bookmarks.clear();
//
//			if (format.equals(WORD))
//			{
//				doc.save(os, SaveFormat.DOC);
//			}
//
//			if (format.equals(PDF))
//			{
//				doc.save(os, SaveFormat.PDF);
//
//				/** 读取书签  **/
//				List<PdfBookModel> allTitle = getAllTitle(doc);
//				/** 创建书签**/
//				AsposePdfUtil.pdfAddMdMark(allTitle, desc_file);
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		} finally
//		{
//			try
//			{
//				os.close();
//			} catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
//	/**
//	 * 生成完整的 swagger 文档 (api概述 + swagger接口内容 )
//	 *
//	 * @param documents 原文档可以多个
//	 * @param desc_file 目的文件名
//	 * @param format 输出格式
//	 */
//	public static void generateCompleteDocx(List<Document> documents, File desc_file, String format){
//
//		FileOutputStream os = null;
//		try
//		{
//			Document doc = new Document();
//			os = new FileOutputStream(desc_file);
//			SetDocxConf instance = SetDocxConf.getInstance();
//			/** 生成封面 */
//			Map<String, String> index = new HashMap<>();
//			index.put(SetDocxConf.INDEX_TIME, DateUtils.now(null));
//			index.put(SetDocxConf.INDEX_EMAIL, docxConf.getEmail());
//			index.put(SetDocxConf.INDEX_URL, docxConf.getUrl());
//			index.put(SetDocxConf.INDEX_VERSIONDOCX, instance.getVersionNum());
//			DocumentBuilder builder = generateCover(doc, index);
//
//			/** 追加其他文档**/
//			int size = documents.size();
//			for (int i = 1; i < size; i++)
//			{
//				Document newDoc = documents.get(i);
//				updateTitleLevel(newDoc,i);
//				doc.appendDocument(newDoc, ImportFormatMode.USE_DESTINATION_STYLES);
//			}
//			doc.appendDocument(documents.get(0), ImportFormatMode.USE_DESTINATION_STYLES);
//
//			/** 添加页码 **/
//			addHeaderFooter(doc);
//			/** 添加目录 **/
//			generateDirectory(doc);
//			/** 删除文档现有的所以书签*/
//			BookmarkCollection bookmarks = doc.getRange().getBookmarks();
//			bookmarks.clear();
//
//			if (format.equals(WORD))
//			{
//				doc.save(os, SaveFormat.DOC);
//			}
//
//			if (format.equals(PDF))
//			{
//				doc.save(os, SaveFormat.PDF);
//
//				/** 读取书签  **/
//				List<PdfBookModel> allTitle = getAllTitle(doc);
//				/** 创建书签**/
//				AsposePdfUtil.pdfAddMdMark(allTitle, desc_file);
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		} finally
//		{
//			try
//			{
//				os.close();
//			} catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
//
//
//	/**-------------文档操作部分--------------------------------------------------------------------------------------------- */
//
//	/**
//	 * 修改文档的标题层级
//	 * @param document
//	 * @return
//	 */
//	private static Document updateTitleLevel(Document document,Integer sequence)
//	{
//
//		try
//		{
//			ParagraphCollection paragraphs = document.getFirstSection().getBody().getParagraphs();
//			Paragraph[] paragraphList = paragraphs.toArray();
//			for (Paragraph nodes : paragraphList)
//			{
//				int level = nodes.getParagraphFormat().getOutlineLevel();
//				Boolean sign = true ;
//				String text = nodes.getText();
//				if(level == 0){
//					nodes.getParagraphFormat().setStyleIdentifier(StyleIdentifier.HEADING_2);
//				}else if(level ==1){
//					nodes.getParagraphFormat().setStyleIdentifier(StyleIdentifier.HEADING_3);
//				}else if(level ==2){
//					nodes.getParagraphFormat().setStyleIdentifier(StyleIdentifier.HEADING_4);
//				}else {
//					sign = false ;
//				}
//
//				if(sign){
//					int newLevel = nodes.getParagraphFormat().getOutlineLevel();
//					log.info("设置  原来标题级别：{} ,现在标题级别：{} ,内容：{}",level ,newLevel,text);
//				}
//			}
//
//			if(sequence ==1){
//				/** 插入一级标题 */
//				DocumentBuilder builder = new DocumentBuilder(document);
//				builder.moveToDocumentStart();
//				builder.writeln();
//				builder.moveToDocumentStart();
//				/** 清楚所有格式，并重新设置**/
//				builder.getParagraphFormat().clearFormatting();
//				builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
//				builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
//				builder.getParagraphFormat().setLineSpacing(18);
//				builder.getFont().clearFormatting();
//				builder.getFont().setColor(Color.BLACK);
//				builder.getFont().setName(docxConf.getTextContentFont());
//				builder.getFont().setSize(docxConf.getTextFirstTitleFontSize());
//				builder.getFont().setBold(true);
//				builder.getParagraphFormat().setStyleIdentifier(StyleIdentifier.HEADING_1);
//				builder.write(docxConf.getAsOverviewFirstTitle());
//			}
//
//		}catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		return document;
//	}
//
//	/**
//	 * 生成封面
//	 * @param index
//	 * @param doc
//	 * @return
//	 */
//	private static DocumentBuilder generateCover(Document doc, Map<String, String> index)
//	{
//		DocumentBuilder builder = null;
//		try
//		{
//			builder = new DocumentBuilder(doc);
//			builder.moveToDocumentStart();
//			/** 0.设置段落字体格式 */
//			builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
//			builder.getParagraphFormat().setLineSpacing(18);
//			builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);
//			builder.getFont().setName(docxConf.getIndexFont());
//			builder.getFont().setColor(Color.BLACK);
//			builder.getFont().setBold(true);
//			builder.getFont().setSize(docxConf.getIndexTitleFontSize());
//			builder.writeln();
//			/**1.插入图片和首页大标题**/
//			InputStream genewImageInputStream = AsposeWordUtil.class.getClassLoader().getResourceAsStream("genew.png");
//			builder.insertImage(genewImageInputStream, RelativeHorizontalPosition.MARGIN, 0, RelativeVerticalPosition.MARGIN, 60.0, 70.0, 75.0,
//					WrapType.TIGHT);
//			genewImageInputStream.close();
//			builder.write(docxConf.getIndexTitleName());
//			/**2.插入首页副标题**/
//			builder.writeln();
//			builder.writeln();
///*            builder.getFont().setSize(docxConf.getIndexSubTitleFontSize());
//        builder.writeln(docxConf.getIndexSubTitleName() );*/
//			builder.writeln();
//			builder.writeln();
//			/**3.插入描述信息**/
//			builder.getParagraphFormat().clearFormatting();
//			builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
//			builder.getParagraphFormat().setLineSpacing(24);
//			builder.getParagraphFormat().setLeftIndent(70);
//			builder.getFont().setSize(docxConf.getIndexDescFontSize());
//
//			String versionNum = index.get(SetDocxConf.INDEX_VERSIONDOCX);
//			if(!StringUtils.isEmpty(versionNum)){
//				builder.writeln(docxConf.getDocxVersion().concat(versionNum));
//			}
//			builder.writeln(docxConf.getContactEmail().concat(index.get(SetDocxConf.INDEX_EMAIL)));
//			builder.writeln(docxConf.getContactUrl().concat(index.get(SetDocxConf.INDEX_URL)));
//			builder.writeln(docxConf.getDocxTime().concat(index.get(SetDocxConf.INDEX_TIME)));
//
//			/**4.在下一页设置目录*/
//			builder.insertBreak(BreakType.PAGE_BREAK);
//			builder.startBookmark("目录");
//			builder.endBookmark("目录");
//        /*Section section = new Section(doc);
//        doc.appendChild(section);*/
//
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		return builder;
//	}
//
//	/**
//	 * 创建目录
//	 *
//	 * @param doc
//	 * @return
//	 */
//	public static Document generateDirectory(Document doc)
//	{
//		try
//		{
//			//addHeaderFooter(doc);
//			DocumentBuilder builder = new DocumentBuilder(doc);
//			//doc.getFirstSection().getBody().prependChild(new Paragraph(doc));
//			//在模板中设置书签可以跳转到书签位置
//			builder.moveToBookmark("目录");
//
//			//设置目录的格式
//			//“目录”两个字居中显示、加粗、搜宋体
//			builder.getParagraphFormat().clearFormatting();
//			builder.getCurrentParagraph().getParagraphFormat().setAlignment(com.aspose.words.ParagraphAlignment.CENTER);
//			builder.setBold(true);
//			builder.getFont().setName("宋体");
//			builder.writeln("目录");
//			//清除所有样式设置
//			builder.getParagraphFormat().clearFormatting();
//			//目录居左
//			builder.getParagraphFormat().setAlignment(com.aspose.words.ParagraphAlignment.LEFT);
//			//插入目录，这是固定的
//			builder.insertTableOfContents("\\o \"1-3\" \\h \\z \\u");
//			/** 更新域 */
//			doc.updateFields();
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		return doc;
//	}
//
//	/**
//	 * 添加页码
//	 *
//	 * @param doc
//	 * @throws Exception
//	 */
//	public static void addHeaderFooter(Document doc) throws Exception
//	{
//		/** 创建页脚页码 **/
//		HeaderFooter footer = new HeaderFooter(doc, HeaderFooterType.FOOTER_PRIMARY);
//
//		/** 设置页码 从指定节开始**/
//		Section sec = doc.getSections().get(1);
//		sec.getPageSetup().setRestartPageNumbering(true);
//		sec.getPageSetup().setPageStartingNumber(1);
//		sec.getHeadersFooters().add(footer);
//
//		/** 页脚段落 添加域值*/
//		Paragraph footerpara = new Paragraph(doc);
//		footerpara.getParagraphFormat().setAlignment(com.aspose.words.ParagraphAlignment.CENTER);
//		Run footerparaRun = new Run(doc);
//		footerparaRun.getFont().setName("宋体");
//		//小5号字体
//		footerparaRun.getFont().setSize(9.0);
//		footerpara.appendChild(footerparaRun);
//		//当前页码
//		footerpara.appendField(FieldType.FIELD_PAGE, true);
//		//footerpara.appendChild(footerparaRun);
//		footer.appendChild(footerpara);
//	}
//
//	/**
//	 * 获得所有文档标题 （ 以便之后用来生成PDF的标签 ）
//	 *
//	 * @param originDoc
//	 * @return
//	 */
//	public static List<PdfBookModel> getAllTitle(Document originDoc)
//	{
//
//		//用来存放所有标题，一级标题集合
//		List<PdfBookModel> data = new ArrayList<>();
//		try
//		{
//
//			// 后面需要用这个对象去获取当前段落所在的页码
//			LayoutCollector layoutCollector = new LayoutCollector(originDoc);
//			// 需要获取所有的section，要不然部分word提取目录不完整
//			Section[] sections = originDoc.getSections().toArray();
//
//			for (int i = 0; i < sections.length; i++)
//			{
//				if (i == 0)
//				{
//					continue;
//				}
//
//				List<Paragraph> paragraphs = Arrays.asList(sections[i].getBody().getParagraphs().toArray());
//
//				for (Paragraph p : paragraphs)
//				{
//					/** 层级 **/
//					int level = p.getParagraphFormat().getOutlineLevel();
//					if (level < 0 || level > 6)
//					{
//						continue;
//					}
//
//					/** 页码*/
//					int pageIndex = layoutCollector.getEndPageIndex(p);
//
//					/** 标题内容 **/
//					String text = "";
//					for (Run run : p.getRuns())
//					{
//						text += run.getText();
//					}
//					if (text == "")
//					{
//						continue;
//					}
//
//					/** 装填标签Model **/
//					PdfBookModel model = new PdfBookModel();
//					model.setLevel(level);
//					model.setTitle(text);
//					model.setPage(pageIndex);
//
//					/** 装填标签Model **/
//					if (level == 0)
//					{
//						data.add(model);
//					} else
//					{
//						getChildTitle(data.get(data.size() - 1), model);
//					}
//				}
//
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		return data;
//	}
//
//	private static void getChildTitle(PdfBookModel parent, PdfBookModel child)
//	{
//		if (parent.getLevel() + 1 == child.getLevel())
//		{
//			parent.getChild().add(child);
//		} else
//		{
//			/** 判断是否出现断层，没有直接下级,建一个空的model填充，保证健壮性（后边删减） 0  , 3 ,3 ,4,,5  */
//			int size = parent.getChild().size();
//			if (size == 0)
//			{
//				parent.getChild().add(new PdfBookModel(parent.getLevel() + 1, "", parent.getPage(), new ArrayList<>()));
//			}
//
//			getChildTitle(parent.getChild().get(parent.getChild().size() - 1), child);
//		}
//	}
//
//
//	/** --------------------测试 main类--------------------------------------------------------------------------------------------------------*/
//	/**
//	 * 直接调用python 将md生成word
//	 */
//	public static void main(String[] args)
//	{
//
//		/** 调用python 将md生成word */
///*        File file = new File(docxConf.getMdToWordFilePath());
//        String descDir = file.getAbsolutePath().replace("\\","/");
//        String pythonPath = MdToWordUtil.class.getClassLoader().getResource("./doc/MdToWord.py").getPath();
//        String sourDir = MdToWordUtil.class.getClassLoader().getResource("./doc").getPath();
//        OsInfo osInfo = SystemUtil.getOsInfo();
//        if(osInfo.isWindows()){
//            pythonPath = pythonPath.substring(1);
//            sourDir = sourDir.substring(1);
//        }
//
//        System.out.println(pythonPath);
//        System.out.println(sourDir);
//        System.out.println(descDir);*/
//
///*        String pythonPath ="D:/GitRep/NUMAX/nuas_new_dev/nuas-rest/build/resources/main/doc/MdToWord.py";
//        String sourDir = "D:/GitRep/NUMAX/nuas_new_dev/nuas-rest/src/main/resources/doc/internal";
//        String descDir = "D:/var/numax/files/nuas-rest-swagger/mdToWord";
//        MdToWordUtil.getInstance().mdFileToWordFile(pythonPath ,sourDir ,descDir);*/
//
///*        List<File> fileList = FileUtils.getFileListByExt("D:\\var\\numax\\files\\nuas-rest-swagger\\mdToWord", true,"md");
//        System.out.println(fileList);*/
//
//		//mdToWordMain("pdf","./doc");
//
///*		String sour_file = "D:\\var\\numax\\files\\nuas-rest-swagger\\mdToWord\\erds";
//		File desc_file = new File("D:\\var\\numax\\files\\nuas-rest-swagger\\merge\\erds.pdf");
//		mergeDocument(sour_file, desc_file, "pdf");*/
//
//	}
//
//	/**
//	 * 生成as接口文档
//	 * @param wordFile  正文内容
//	 * @param sourcePath api概述 markdown 源路径
//	 * @param isIncludeSubDir 如果有下级目录，是否包括下级目录的markdown
//	 * @param isMerge 该目录下有多个markdown ，是不是要合并
//	 * @param format 最后新文件的输出格式 ,支持 docx/pdf 格式
//	 */
//	public static File generateCompleteFile(File wordFile, String sourcePath, boolean isIncludeSubDir, boolean isMerge, String format,String filename)
//	{
//
//		/** 1. 调用pandoc  将指定目录下的 md 转为 word **/
//		String descPath = docxConf.getMdToWordFilePath();
//		mdFileToWordFile(sourcePath, descPath ,isIncludeSubDir ,isMerge);
//
//		/** 2. 以指定路径下的 目录为文件名，合并该目录下的所有word文件 ，为一个新的文件 */
//		List<File> fileList = new ArrayList<>();
//		List<String> dirs = FileUtils.getAllFileName(descPath , true);
//		for (String mdSourceDir : dirs)
//		{
//			/** 2.1 获得新的文件名 **/
//			log.info("process merged directory : " + mdSourceDir);
//			String[] path = null ;
//			OsInfo osInfo = SystemUtil.getOsInfo();
//			if (osInfo.isWindows()){
//				path = mdSourceDir.split("\\\\");
//			}else if(osInfo.isLinux()){
//				path = mdSourceDir.split("/");
//			}else{
//				//预留
//				log.info("This interface currently only supports window and linux servers running");
//			}
//
//			String mdDir = path[path.length - 1];
//			String desc_file_dir = "";
//			String desc_file_name = filename;
//			if (WORD.contains(format))
//			{
//				desc_file_name += ".docx";
//				desc_file_dir = docxConf.getWordFilePath() + "/"+mdDir;
//			}
//			if (format.equals(PDF))
//			{
//				desc_file_name += ".pdf";
//				desc_file_dir = docxConf.getPdfFilePath()+ "/"+mdDir;
//			}
//
//			File desc_file = new File(desc_file_dir, desc_file_name);
//			FileUtils.createFile(desc_file);
//
//			/** 2.2 合并多个word文档**/
//			String[] sour_absolute_path = FileUtils.getFileAllPathByExt(mdSourceDir, true ,"docx");
//
//			if(sour_absolute_path.length == 0 ){
//				log.error("生成NUAS接口文档：未找到 md转为的word,无法将quarkStart接口信息合并到文档中: 请检查你是否安装pandoc");
//			}
//			List<Document> documents = new ArrayList<>();
//			try
//			{
//				/** as的api具体内容 **/
//				documents.add(new Document(wordFile.getAbsolutePath()));
//				/** 要合并的各个 api概述 word文档**/
//				for (String sourceName : sour_absolute_path)
//				{
//					documents.add(new Document(sourceName));
//				}
//
//			} catch (Exception e)
//			{
//				e.printStackTrace();
//			}
//			generateCompleteDocx(documents , desc_file ,format);
//
//			fileList.add(desc_file);
//		}
//		/** 删除md转为word的单个文件，避免新增或减少md，但合并时仍老路径下仍有原docx文件，导致多余合并**/
//		FileUtils.deleteAllFile(descPath);
//		return fileList.get(0);
//
//	}
//
//	/**
//	 * @param pythonFile python文件路径
//	 * @param sourceDir  待处理的md文件所在目录
//	 * @param descDir    处理完的word存放根目录
//	 * @Description: 该函数 调用python文件处理 将指定文件夹下的md文档转为word文档
//	 */
//
//	public void javaCtrPython(String pythonFile, String sourceDir, String descDir)
//	{
//		try
//		{
//			log.info("start markdown convert to word");
//
//			String[] parameter = new String[] { "python", pythonFile, sourceDir, descDir };
//			Process pr = Runtime.getRuntime().exec(parameter);
//			BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//			String line;
//			while ((line = in.readLine()) != null)
//			{
//				log.info(line);
//			}
//			in.close();
//			pr.waitFor();
//			log.info("end markdown convert to word");
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
//}
