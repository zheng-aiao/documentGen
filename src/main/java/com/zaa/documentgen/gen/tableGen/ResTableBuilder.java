package com.zaa.documentgen.gen.tableGen;

import com.aspose.words.*;
import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.constants.TableConst;
import com.zaa.documentgen.gen.textBuilder.TextBuilder;

import java.awt.*;
import java.util.List;
import java.util.*;


public class ResTableBuilder {


    /**
     * 功能：生成响应参数表格
     *
     * @param builder
     * @param res
     */
    public static void addResParamTable(DocumentBuilder builder, List<Map<String, Object>> res)
    {
        try
        {

            Table table = builder.startTable();
            /** 插入表头 **/
            addTableRowHeader(builder);

            List<Map<String, Object>> subParamList = new ArrayList<>();
            Set<String> typeSets = new HashSet<>();
            /** 插入表单元格内容*/
            for (int j = 0; j < res.size(); j++) {
                //获取map数据
                Map<String, Object> mapRes = res.get(j);
                addResTableRowContent(builder , mapRes);

                //属性为复杂类型，需要创建子表格
                List<Map<String, Object>> sublist = (List<Map<String, Object>>) mapRes.get(DocxConf.PARAM_SUB_LIST);
                if( sublist != null && sublist.size() > 0){
                    String dataType = (String) mapRes.get(DocxConf.PARAM_DATA_TYPE);
                    if(!typeSets.contains(dataType)){
                        typeSets.add(dataType);
                        subParamList.add(mapRes);
                    }

                }
            }
            table.autoFit(AutoFitBehavior.FIXED_COLUMN_WIDTHS);
            builder.endTable();
            addResParamSubTable(builder, typeSets, subParamList);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 递归生成响应子表格
     * 传入一级表格里有下一级的参数列表，并递归展开所有参数，每个model展开为一个表格
     * @param builder
     * @param modelNameSets
     * @param subParamList
     */

    private static void addResParamSubTable(DocumentBuilder builder, Set<String> modelNameSets , List<Map<String, Object>> subParamList){
        try
        {
            /** 用来存放下一级，仍需要遍历展开为表格的参数*/
            List<Map<String, Object>> subParam = new ArrayList<>();
            List<Map<String, Object>> statusParam = new ArrayList<>();

            for (Map<String, Object> parentParamList : subParamList)
            {
                /** 对data 与 status 做不同处理 **/
                List<Map<String, Object>> resParamList = (List<Map<String, Object>>) (parentParamList.get(DocxConf.PARAM_SUB_LIST));
                String paramDataType = parentParamList.get(DocxConf.PARAM_DATA_TYPE).toString();


//                String paramName = parentParamList.get(DocxConf.PARAM_NAME).toString();
//                if(paramName.equals("status")){
//                    statusParam = (List<Map<String, Object>>) parentParamList.get(DocxConf.PARAM_SUB_LIST);
//                    continue;
//                }else{
//                    builder.writeln();
//                }

                // 填写介绍
                TextBuilder.addTableIntroduce(builder, paramDataType);


                Table table = builder.startTable();
                addTableRowHeader(builder);

                /** 插入表单元格内容*/
                for (int j = 0; j < resParamList.size(); j++) {
                    //获取map数据
                    Map<String, Object> mapReq = resParamList.get(j);
                    addResTableRowContent(builder , mapReq);

                    if( ((List)mapReq.get(DocxConf.PARAM_SUB_LIST)).size() != 0){
                        String dataType = (String) mapReq.get(DocxConf.PARAM_DATA_TYPE);
                        if(!modelNameSets.contains(dataType)){
                            modelNameSets.add(dataType);
                            subParam.add(mapReq);
                        }
                    }
                }
                table.autoFit(AutoFitBehavior.FIXED_COLUMN_WIDTHS);
                builder.endTable();
            }

            /** 考虑递归 展开下一级**/
            if(subParam.size() > 0){
                addResParamSubTable(builder, modelNameSets, subParam);
            }

            /** 把状态码放到最后 **/
            /** 插入表头 **/
//            if(statusParam.size() > 0){
//                builder.writeln();
//                builder.getFont().setSize(instance.getTextFontSize());
//                builder.getFont().setBold(false);
//                builder.getFont().setName(instance.getTextContentFont());
//                builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
//                builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.MULTIPLE);
//                builder.getParagraphFormat().setLineSpacing(18);
//                builder.writeln(instance.getInterResStatus());
//
//                Table table = builder.startTable();
//                addTableRowHeader(builder,instance);
//                for (Map<String, Object> statusMap : statusParam)
//                {
//                    addResTableRowContent(builder , statusMap);
//                }
//                table.autoFit(AutoFitBehavior.FIXED_COLUMN_WIDTHS);
//                builder.endTable();
//            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public static void addTableRowHeader(DocumentBuilder builder)
    {
        try
        {
            /** 设置格式 **/
            builder.getRowFormat().clearFormatting();
            builder.getRowFormat().setHeightRule(HeightRule.EXACTLY);
            builder.getRowFormat().setHeight(30.0);

            builder.getCellFormat().clearFormatting();
            builder.getCellFormat().setVerticalAlignment(CellVerticalAlignment.CENTER);
            builder.getCellFormat().getShading().setBackgroundPatternColor(Color.decode(TableConst.tableHeaderBackgroundColor));

            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setLineSpacingRule(LineSpacingRule.AT_LEAST);
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.CENTER);

            builder.getFont().clearFormatting();
            builder.getFont().setBold(true);
            builder.getFont().setSize(TableConst.tableHeaderFontSize);
            builder.getFont().setName(TableConst.tableHeaderFontName);
            builder.getFont().setColor(Color.decode(TableConst.tableHeaderFontColor));


            /** 插入表头*/
            builder.insertCell();
            builder.getCellFormat().setWidth(150);
            builder.write(DocxConf.PARAM_NAME);
            builder.insertCell();
            builder.getCellFormat().setWidth(100);
            builder.write(DocxConf.PARAM_DATA_TYPE);
            builder.insertCell();
            builder.getCellFormat().setWidth(200);
            builder.write(DocxConf.PARAM_DESC);
            builder.endRow();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void addResTableRowContent(DocumentBuilder builder, Map<String, Object> mapRes)
    {
        try
        {
            /** 设置格式 **/
            builder.getRowFormat().clearFormatting();
            builder.getRowFormat().setHeightRule(HeightRule.AUTO); // 自动行高

            builder.getCellFormat().clearFormatting();
            builder.getCellFormat().setVerticalAlignment(CellVerticalAlignment.CENTER);
            builder.getCellFormat().getShading().setBackgroundPatternColor(Color.decode(TableConst.tableContentBackgroundColor));


            builder.getParagraphFormat().clearFormatting();
            builder.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT); // 数据左对齐
            builder.getParagraphFormat().setSpaceBefore(6.0);
            builder.getParagraphFormat().setSpaceAfter(6.0);

            builder.getFont().clearFormatting();
            builder.getFont().setBold(false); // 取消加粗
            builder.getFont().setName(TableConst.tableContentFontName);
            builder.getFont().setSize(TableConst.tableContentFontSize); // 数据字体大小可能不同
            builder.getFont().setColor(Color.decode(TableConst.tableContentFontColor));

            /** 读取数据*/
            String resName = mapRes.get(DocxConf.PARAM_NAME).toString();
            String resDataType = mapRes.get(DocxConf.PARAM_DATA_TYPE).toString();
            String resDesc = mapRes.get(DocxConf.PARAM_DESC).toString();


            /** 插入表格内容*/
            builder.getFont().setName(TableConst.tableEnglishContentFontName);
            builder.insertCell();
            builder.getCellFormat().setWidth(150);
            builder.write(resName);
            builder.insertCell();
            builder.getCellFormat().setWidth(100);
            builder.write(resDataType);
            builder.getFont().setName(TableConst.tableContentFontName);
            builder.insertCell();
            builder.getCellFormat().setWidth(200);
            builder.write(resDesc);
            builder.endRow();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
