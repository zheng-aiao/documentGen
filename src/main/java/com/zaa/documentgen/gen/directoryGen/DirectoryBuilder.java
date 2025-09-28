package com.zaa.documentgen.gen.directoryGen;

import com.aspose.words.*;

public class DirectoryBuilder {

    public static void insertDirMark(DocumentBuilder builder, Document doc) {
        try {
            builder.insertBreak(BreakType.PAGE_BREAK);
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);
            builder.getFont().setName("宋体");
            builder.setBold(true);
            builder.writeln("目录");

            builder.startBookmark("目录");
            builder.endBookmark("目录");
            Section section = new Section(doc);
            doc.appendChild(section);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 三、创建目录和页码
     *
     * @param doc
     * @return
     */
    public static Document generateDirectory(Document doc) {
        try {
            // 在模板中设置书签可以跳转到书签位置
            DocumentBuilder builder = new DocumentBuilder(doc);
            // 移到书签结束位置之后，在“目录”标题下面插入 TOC
            builder.moveToBookmark("目录", false, true);
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
            builder.insertTableOfContents("\\o \"1-2\" \\h \\z \\u");
            doc.updateFields();
            builder.insertBreak(BreakType.PAGE_BREAK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * 在最后一节添加页脚页码
     *
     * @param doc
     * @throws Exception
     */
    public static void addHeaderFooter(Document doc) throws Exception {
        // 1. 创建页脚对象
        HeaderFooter footer = new HeaderFooter(doc, HeaderFooterType.FOOTER_PRIMARY);
        doc.getLastSection().getHeadersFooters().add(footer);

        // 2. 重置页码（从1开始）
        doc.getLastSection().getPageSetup().setRestartPageNumbering(true);
        doc.getLastSection().getPageSetup().setPageStartingNumber(1);

        // 3. 构建页脚段落
        Paragraph footerPara = new Paragraph(doc);
        footerPara.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);

        // 4. 设置字体样式
        Run run = new Run(doc);
        run.getFont().setName("宋体");
        run.getFont().setSize(9.0);
        footerPara.appendChild(run);

        // 5. 插入页码字段
        footerPara.appendField(FieldType.FIELD_PAGE, true);

        // 6. 将段落添加到页脚
        footer.appendChild(footerPara);
    }
}
