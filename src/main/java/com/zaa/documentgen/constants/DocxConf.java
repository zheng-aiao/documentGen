package com.zaa.documentgen.constants;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

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
@Getter
@Setter
public class DocxConf implements Serializable
{
    private static DocxConf setDocxConf = new DocxConf();
    public static DocxConf getInstance() {
        return setDocxConf;
    }
    private DocxConf() {

    }


    /**
     * ============================================文档常量及配置信息======================================================
     */
    private String name = "";
    private String url = "https://www.genew.com.cn/";
    private String email = "info@genew.com";

    /**
     * md-> pdf/word默认的文件导出路径（此处为名，路径写法：XXX/XXX/XXX）
     */
    public static  String genFilePath = "/var/numax";
    private String pdfFilePath = "/var/numax/files/nuas-rest-swagger/pdf";
    private String mdMergeFilePath = "/var/numax/files/nuas-rest-swagger/merge";
    private String mdToWordFilePath = "/var/numax/files/nuas-rest-swagger/mdToWord";
    private String quickstartFilePath = "/usr/local/nuas-rest/conf/doc/quickstart";

    /**
     * 默认说明
     */
    private String wordDesc = "NUAS接口文档说明书（word版本）";
    private String pdfDesc = "NUAS接口文档说明书（pdf版本）";
    private String asOverviewFirstTitle = "1、NUAS接口规范";
    private String asContentFirstTitle = "2、NUAS开放接口";
    private String versionNum = "";
    /**
     * 水印内容
     */
    private String watermarkText = "";
    /**
     * 首页介绍的 key （一级标题：固定名称）
     */
    private String indexTitleName = "深圳震有科技股份有限公司";
    private String indexSubTitleName = "NuMax ";
    private String docxVersion = "文档版本：";
    private String contactEmail = "震有邮件：";
    private String contactUrl = "官方网址：";
    private String docxTime = "创建时间：";

    /**
     * 文档的创建时间可以自己定义，不定义的时候取当前时间为准
     */
    private String docxTimeValue = null;

    /**
     * 避免标题重复的 标题分隔符
     */
    private String splitTitle = "@@@WE-";


    /**
     * 接口文档每个接口各部分前缀
     */
    private String interDesc = "接口描述";
    private String interUrl = "接口地址";
    private String interMethod = "请求方式";
    private String interType = "请求类型";
    private String interTypeRes = "响应类型";
    private String interReq = "请求参数";
    private String interRes = "响应参数";
    private String interResStatus = "status 参数说明:";
    private String interExample = "请求示例";
    /**
     * 请求示例中表格的列内容
     */
    private String interReqExample = "请求参数示例：";
    private String interResExample = "响应参数示例：";



    /**
     * ============================================字体设置；==================================================================
     */
    /**
     *  封面字体
     */
    private String indexFont = "微软雅黑";

    /**
     *  内容字体
     */
    private String textContentFont = "宋体";
    /**
     *  表格字体
     */
    private String textTableFont = "黑体";
    /**
     * 英文及数字字体
     */
    private String textEnglishFont = "Times New Roman";
    /**
     *  首页大标题字号
     */
    private int indexTitleFontSize = 28;

    /**
     *  首页副标题字号
     */
    private int indexSubTitleFontSize = 26;
    /**
     *  首页描述信息字号
     */

    private int indexDescFontSize = 16;
    /**
     * 文档1级标题大小
     */
    private int textFirstTitleFontSize = 22;
    /**
     * 文档2级标题大小
     */
    private int textSecondTitleFontSize = 18;
    /**
     * 文档3级标题大小
     */
    private int textThreeTitleFontSize = 14;
    /**
     * 文档4级标题大小
     */
    private int textFourTitleFontSize = 10;
    /**
     * 正文文本大小
     */
    private int textFontSize = 12;
    /**
     * 表头字体
     */
    private int tableHeaderFontSize = 11;
    /**
     * 表内容字体
     */
    private Double tableContentFontSize = 10.5d;


    /**
     * ============================================颜色设置；=================================================================
     */
    /**
     * 标题颜色
     */
    private String textTitleColor = "000000";
    /**
     * 请求参数列表行头颜色
     */
    private String reqRowColor = "#CDC9C9";
    /**
     * 请求参数列表行体颜色
     */
    private String reqBodyColor = "#4b7ab3";
    /**
     * 响应参数列表行头颜色
     */
    private String resRowColor = "#CDC9C9";
    /**
     * 响应参数列表行体颜色
     */
    private String resBodyColor = "#FFFFFF";
    /**
     * 请求示例 背景色
     */
    private String reqExampleRowColor = "#CDC9C9";
    /**
     * 请求示例 背景色
     */
    private String reqExampleCellColor = "#FFFFFF";
    /**
     * 响应示例 背景色
     */
    private String resExampleRowColor = "#CDC9C9";
    /**
     * 响应示例 背景色
     */
    private String resExampleCellColor = "#FFFFFF";

    /**
     * ============================================解析渲染 map键 常量值；=================================================================
     */

    /**
     * 首页目录信息定义魔法值
     */
    public final static String HOME_TITLE = "title";
    public final static String HOME_DESC = "desc";
    public final static String HOME_VERSIONSWAGGER = "version_swagger";
    public final static String HOME_VERSIONDOCX = "version_docx";
    public final static String HOME_NAME = "name";
    public final static String HOME_URL = "url";
    public final static String HOME_EMAIL = "email";
    public final static String HOME_TIME = "time";


    /**
     * 接口信息 定义魔法值
     */
    public final static String INTERFACE_NAME = "name";
    public final static String INTERFACE_DESC = "desc";
    public final static String INTERFACE_URL = "url";
    public final static String INTERFACE_METHOD = "method";
    //请求响应： 参数，例子，类型
    public final static String INTERFACE_REQ = "req";
    public final static String INTERFACE_RES = "res";
    public final static String INTERFACE_REQ_EXAMPLE = "req_example";
    public final static String INTERFACE_RES_EXAMPLE = "res_example";
    public final static String INTERFACE_TYPE = "type";
    public final static String INTERFACE_TYPE_RES = "type_res";
    public final static String INTERFACE_ORDER = "order";


    /**
     * 接口参数 列名
     */
    public final static String PARAM_NAME = "参数名称";
    public final static String PARAM_DATA_TYPE = "数据类型";
    public final static String PARAM_REQ_TYPE = "参数类型";
    public final static String PARAM_REQ_ISFILL = "是否必填";
    public final static String PARAM_DESC = "参数说明";
    public final static String PARAM_SUB_LIST = "请求子参数";

}
