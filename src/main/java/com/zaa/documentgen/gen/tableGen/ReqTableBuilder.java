package com.zaa.documentgen.gen.tableGen;

import com.aspose.words.*;
import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.constants.TableConst;
import com.zaa.documentgen.gen.textBuilder.TextBuilder;

import java.awt.*;
import java.util.List;
import java.util.*;


public class ReqTableBuilder{

    /**
     * 功能：生成请求参数表格
     *
     * @param builder
     * @param req
     */
    public static void addReqParamTable(DocumentBuilder builder,  List<Map<String, Object>> req)
    {
        try
        {
            // 设置表格头格式
            Table table = builder.startTable();
            addReqTableRowHeader(builder);


            List<Map<String, Object>> subParamList = new ArrayList<>();
            Set<String> typeSets = new HashSet<>();

            /** 插入表单元格内容*/
            for (int j = 0; j < req.size(); j++) {
                //获取map数据
                Map<String, Object> mapReq = req.get(j);
                addReqTableRowContent(builder , mapReq);

                //属性为复杂类型，需要创建子表格
                List<Map<String, Object>> sublist = (List<Map<String, Object>>) mapReq.get(DocxConf.PARAM_SUB_LIST);
                if( sublist != null && sublist.size() > 0){
                    String dataType =  mapReq.get(DocxConf.PARAM_DATA_TYPE) == null? "" : mapReq.get(DocxConf.PARAM_DATA_TYPE).toString();
                    if(!typeSets.contains(dataType)){
                        typeSets.add(dataType);
                        subParamList.add(mapReq);
                    }

                }

            }
            table.autoFit(AutoFitBehavior.FIXED_COLUMN_WIDTHS);
            builder.endTable();
            addReqParamSubTable(builder, typeSets, subParamList);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 递归生成请求子表格
     * 传入一级表格里有下一级的参数列表，并递归展开所有参数，每个model展开为一个表格
     * @param builder
     * @param subParamList
     */
    private static void addReqParamSubTable(DocumentBuilder builder, Set<String> modelNameSets , List<Map<String, Object>> subParamList){
        try
        {
            /** 用来存放下一级，仍需要遍历展开为表格的参数*/
            List<Map<String, Object>> subParam = new ArrayList<>();

            for (Map<String, Object> parentParamList : subParamList)
            {
                List<Map<String, Object>> reqParamList = (List<Map<String, Object>>) (parentParamList.get(DocxConf.PARAM_SUB_LIST));
                String paramDataType = parentParamList.get(DocxConf.PARAM_DATA_TYPE) == null ? "" : parentParamList.get(DocxConf.PARAM_DATA_TYPE).toString();

                // 填写介绍
                TextBuilder.addTableIntroduce(builder, paramDataType);

                Table table = builder.startTable();
                addReqTableRowHeader(builder);

                /** 插入表单元格内容*/
                for (int j = 0; j < reqParamList.size(); j++) {
                    //获取map数据
                    Map<String, Object> mapReq = reqParamList.get(j);
                    addReqTableRowContent(builder, mapReq);

                    if( ((List)mapReq.get(DocxConf.PARAM_SUB_LIST)).size() != 0){
                        String dataType = parentParamList.get(DocxConf.PARAM_DATA_TYPE) == null ? "" : parentParamList.get(DocxConf.PARAM_DATA_TYPE).toString();
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
                addReqParamSubTable(builder,modelNameSets, subParam);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void addReqTableRowHeader(DocumentBuilder builder)
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
            builder.getCellFormat().setWidth(90);
            builder.write(DocxConf.PARAM_NAME);
            builder.insertCell();
            builder.getCellFormat().setWidth(75);
            builder.write(DocxConf.PARAM_DATA_TYPE);
            builder.insertCell();
            builder.getCellFormat().setWidth(55);
            builder.write(DocxConf.PARAM_REQ_TYPE);
            builder.insertCell();
            builder.getCellFormat().setWidth(55);
            builder.write(DocxConf.PARAM_REQ_ISFILL);
            builder.insertCell();
            builder.getCellFormat().setWidth(175);
            builder.write(DocxConf.PARAM_DESC);
            builder.endRow();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void addReqTableRowContent(DocumentBuilder builder, Map<String, Object> mapReq)
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
            String reqName = mapReq.get(DocxConf.PARAM_NAME).toString();
            String reqDataType = mapReq.get(DocxConf.PARAM_DATA_TYPE) == null ? "" : mapReq.get(DocxConf.PARAM_DATA_TYPE).toString();
            String reqParamType = mapReq.get(DocxConf.PARAM_REQ_TYPE).toString();
            String isFull = mapReq.get(DocxConf.PARAM_REQ_ISFILL).toString();
            String reqDesc = mapReq.get(DocxConf.PARAM_DESC).toString();


            /**  插入内容 */
            builder.getFont().setName(TableConst.tableEnglishContentFontName);
            builder.insertCell();
            builder.getCellFormat().setWidth(90);
            builder.write(reqName);
            builder.insertCell();
            builder.getCellFormat().setWidth(75);
            builder.write(reqDataType);
            builder.insertCell();
            builder.getCellFormat().setWidth(55);
            builder.write(reqParamType);
            builder.getFont().setName(TableConst.tableContentFontName);
            builder.insertCell();
            builder.getCellFormat().setWidth(55);
            builder.write(isFull.equals("false")?"否":"是");
            builder.insertCell();
            builder.getCellFormat().setWidth(175);
            builder.write(reqDesc);
            builder.endRow();


        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
