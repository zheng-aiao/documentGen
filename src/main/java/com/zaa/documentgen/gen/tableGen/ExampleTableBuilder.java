package com.zaa.documentgen.gen.tableGen;

import com.aspose.words.*;
import com.zaa.documentgen.constants.TableConst;

import java.awt.*;
import java.util.Map;

public class ExampleTableBuilder {
    /**
     * 添加请求示例参数内容
     *
     * @param builder
     * @param reqExample
     */
    public static void addReqExample(DocumentBuilder builder , Map<String,String> reqExample){


        try
        {
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setSpaceBefore(6.0);
            builder.getParagraphFormat().setSpaceAfter(6.0);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);

            builder.getFont().clearFormatting();
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setSize(TableConst.tableContentFontSize);
            builder.getFont().setName(TableConst.tableEnglishContentFontName);

            Table table = builder.startTable();
            /** 遍历填写数据 **/
            for (Map.Entry<String, String> entry : reqExample.entrySet())
            {
                /** 请求类型 **/
                builder.insertCell();
                builder.getFont().setBold(true);
                builder.getCellFormat().getShading().setBackgroundPatternColor(Color.decode(TableConst.tableHeaderBackgroundColor));

                builder.getCellFormat().setWidth(45);
                builder.write(entry.getKey());
                /**请求值 **/
                builder.insertCell();
                builder.getCellFormat().getShading().setBackgroundPatternColor(Color.decode(TableConst.tableContentBackgroundColor));
                builder.getFont().setBold(false);
                builder.getCellFormat().setWidth(405);
                builder.write(entry.getValue());
                builder.endRow();
            }
            table.autoFit(AutoFitBehavior.FIXED_COLUMN_WIDTHS);
            builder.endTable();
            builder.writeln();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     *  添加响应示例参数内容
     *
     * @param builder
     * @param resExample
     */
    public static void addResExample(DocumentBuilder builder,  Map<String,String> resExample){
        try
        {
            /** 编写格式 **/
            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
            builder.getParagraphFormat().setSpaceBefore(6.0);
            builder.getParagraphFormat().setSpaceAfter(6.0);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);

            builder.getFont().clearFormatting();
            builder.getFont().setColor(Color.BLACK);
            builder.getFont().setSize(TableConst.tableContentFontSize);
            builder.getFont().setName(TableConst.tableEnglishContentFontName);
            builder.getCellFormat().getShading().setBackgroundPatternColor(Color.decode(TableConst.tableContentBackgroundColor));

            Table table = builder.startTable();
            builder.insertCell();
            builder.getCellFormat().setWidth(450);
            builder.getFont().setBold(false);
            builder.write(resExample.get("response"));
            builder.endRow();

            table.autoFit(AutoFitBehavior.FIXED_COLUMN_WIDTHS);
            builder.endTable();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
