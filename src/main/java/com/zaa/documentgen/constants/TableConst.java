package com.zaa.documentgen.constants;

public interface TableConst {

    /**
     * 请求参数 ： 使用三级标题大小
     */

    /**
     *  请求表格头
     */
    public String tableHeaderFontName = "黑体";
    public Double tableHeaderFontSize =  11d ;
    public String tableHeaderFontColor = "#000000";
    public String tableHeaderBackgroundColor = "#e6e6e6";

    /**
     * 请求表格体（内容）
     */
    public String tableContentFontName = "宋体";
    public String tableEnglishContentFontName = "Times New Roman";
    public Double tableContentFontSize = 10.5d;
    public String tableContentFontColor = "#000000";
    public String tableContentBackgroundColor  = "#FFFFFF";

    /**
     * 各表格说明使用的字体 ep： FileVO 参数说明
     */

    public String tableIntroduceFontName = "宋体";
    public Double tableIntroduceFontSize = 11d;

}
