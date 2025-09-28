package com.zaa.documentgen.gen.textBuilder;

import com.aspose.words.DocumentBuilder;
import com.aspose.words.LineSpacingRule;
import com.aspose.words.ParagraphAlignment;
import com.aspose.words.StyleIdentifier;
import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.constants.InterfaceConst;
import com.zaa.documentgen.constants.TableConst;
import com.zaa.documentgen.constants.TextConst;
import io.micrometer.common.util.StringUtils;

import java.awt.*;
import java.util.Map;

public class TextBuilder {

    /**
     * 功能： 编写一级标题
     *
     * @param builder
     * @param seq
     * @param title
     */
    public static void addFirstTitle(DocumentBuilder builder, String seq, String title) {
        try {
            /** 清楚所有格式，并重新设置 **/
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setStyleIdentifier(StyleIdentifier.HEADING_1);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setOutlineLevel(1);
            builder.getParagraphFormat().setLineSpacing(18);
            // 设置段前间距（如12磅）
            builder.getParagraphFormat().setSpaceBefore(12.0);
            // 设置段后间距（如6磅）
            builder.getParagraphFormat().setSpaceAfter(3.0);
            builder.getFont().clearFormatting();
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setSize(TextConst.textFirstTitleFontSize);
            builder.getFont().setBold(true);
            if (!StringUtils.isEmpty(seq)) {
                builder.getFont().setName(TextConst.titleEnFont);
                builder.write(seq + ".  ");
            }
            builder.getFont().setName(TextConst.titleFont);
            String safeTitle = StringUtils.isEmpty(title) ? "未分组" : title;
            builder.writeln(safeTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能： 编写二级标题
     *
     * @param builder
     * @param seq
     * @param title
     */
    public static void addSecondTitle(DocumentBuilder builder, String seq, String title) {
        try {

            /** 清楚所有格式，并重新设置 **/
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setStyleIdentifier(StyleIdentifier.HEADING_2);
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setLineSpacing(18);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
            // 设置段前间距（如12磅）
            builder.getParagraphFormat().setSpaceBefore(12.0);
            // 设置段后间距（如6磅）
            builder.getParagraphFormat().setSpaceAfter(3.0);
            builder.getFont().clearFormatting();
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setSize(TextConst.textSecondTitleFontSize);
            builder.getFont().setBold(true);
            builder.getFont().setItalic(false);
            if (!StringUtils.isEmpty(seq)) {
                builder.getFont().setName(TextConst.titleEnFont);
                builder.write(seq + ".  ");
            }
            builder.getFont().setName(TextConst.titleFont);
            builder.writeln(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能： 编写三级标题
     *
     * @param builder
     * @param seq
     * @param title
     */
    public static void addThirdTitle(DocumentBuilder builder, String seq, String title) {
        try {

            /** 清楚所有格式，并重新设置 **/
            builder.getParagraphFormat().clearFormatting();
            builder.getFont().clearFormatting();
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setStyleIdentifier(StyleIdentifier.HEADING_3);
            builder.getParagraphFormat().setLineSpacing(18);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
            // 设置段前间距（如12磅）
            builder.getParagraphFormat().setSpaceBefore(12.0);
            // 设置段后间距（如6磅）
            builder.getParagraphFormat().setSpaceAfter(6.0);
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setSize(TextConst.textThreeTitleFontSize);
            builder.getFont().setBold(true);
            builder.getFont().setItalic(false);
            if (!StringUtils.isEmpty(seq)) {
                builder.getFont().setName(TextConst.titleEnFont);
                builder.write(seq + "  ");
            }
            builder.getFont().setName(TextConst.titleFont);
            builder.writeln(title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加接口信息
     *
     * @param builder
     * @param map
     */
    public static void addInterfaceContent(DocumentBuilder builder, Map<String, Object> map) {
        try {
            String paramUrl = map.get(DocxConf.INTERFACE_URL) == null ? "" : map.get(DocxConf.INTERFACE_URL).toString();
            String paramMethod = map.get(DocxConf.INTERFACE_METHOD) == null ? ""
                    : map.get(DocxConf.INTERFACE_METHOD).toString();
            String reqType = map.get(DocxConf.INTERFACE_TYPE) == null ? ""
                    : map.get(DocxConf.INTERFACE_TYPE).toString();
            String resType = map.get(DocxConf.INTERFACE_TYPE_RES) == null ? ""
                    : map.get(DocxConf.INTERFACE_TYPE_RES).toString();
            String paramDesc = map.get(DocxConf.INTERFACE_DESC) == null ? ""
                    : map.get(DocxConf.INTERFACE_DESC).toString();

            addEmptyRow(builder, InterfaceConst.interfaceDescFontSize, 18.0d);
            addInterfaceDescInfo(builder, InterfaceConst.interUrl, paramUrl, "");
            addInterfaceDescInfo(builder, InterfaceConst.interMethod, paramMethod, "");
            addInterfaceDescInfo(builder, InterfaceConst.interReqType, reqType, "");
            addInterfaceDescInfo(builder, InterfaceConst.interResType, resType, "");
            addInterfaceDescInfo(builder, InterfaceConst.interDesc, paramDesc, InterfaceConst.interfaceDescFontName);
            addEmptyRow(builder, InterfaceConst.interfaceDescFontSize, InterfaceConst.interfaceDescFontSize);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 编写接口内容
     *
     * @param builder
     * @param content
     */
    public static void addInterfaceDescInfo(DocumentBuilder builder, String title, String content,
            String contentFontName) {

        try {
            /** 共用配置 */
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setLineSpacing(18);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);

            builder.getFont().clearFormatting();
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setItalic(false);

            /** 填写内容 */
            builder.getFont().setName(InterfaceConst.interfaceDescFontName);
            builder.getFont().setSize(InterfaceConst.interfaceDescFontSize);
            builder.getFont().setBold(true);
            builder.write(title + "：");
            builder.getFont().setName(StringUtils.isEmpty(contentFontName) ? InterfaceConst.interfaceDescEnglishFontName
                    : contentFontName);
            builder.getFont().setSize(InterfaceConst.interfaceDescFontSize);
            builder.getFont().setBold(false);
            builder.writeln(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加空行
     *
     * @param builder
     * @param lineHeight
     */
    public static void addEmptyRow(DocumentBuilder builder, double fontSize, double lineHeight) {

        try {
            /** 共用配置 */
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setLineSpacing(lineHeight);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
            builder.getFont().clearFormatting();
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setItalic(false);
            builder.getFont().setName(InterfaceConst.interfaceDescFontName);
            builder.getFont().setSize(fontSize);
            /** 填写内容 */
            builder.writeln();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 编写表格介绍内容
     *
     * @param builder
     * @param content
     */
    public static void addTableIntroduce(DocumentBuilder builder, String content) {
        try {

            /** 清楚所有格式，并重新设置 **/
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
            builder.getParagraphFormat().setLineSpacing(18);
            builder.getParagraphFormat().setSpaceBefore(12.0);
            builder.getParagraphFormat().setSpaceAfter(6.0);
            builder.getFont().clearFormatting();
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setName(TableConst.tableEnglishContentFontName);
            builder.getFont().setSize(TableConst.tableIntroduceFontSize);
            builder.getFont().setBold(false);
            builder.getFont().setItalic(false);
            builder.write(content);
            builder.getFont().setName(TableConst.tableHeaderFontName);
            builder.writeln(" 参数说明");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 编写正文内容
     *
     * @param builder
     * @param content
     */
    public static void addTextContent(DocumentBuilder builder, String content) {
        try {

            /** 清楚所有格式，并重新设置 **/
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setLineSpacing(12);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);

            builder.getFont().clearFormatting();
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setName(TextConst.textContentFontName);
            builder.getFont().setSize(TextConst.textContentFontSize);
            builder.getFont().setBold(false);
            builder.getFont().setItalic(false);
            builder.writeln(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
