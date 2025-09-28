package com.zaa.documentgen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenewSwaggerHistoryInfo {
    /**
     * 文档名称
     */
    private String name;

    /**
     * 启动时间
     */
    private String startTime;

    /**
     * 版本号
     */
    private String version;

    /**
     * 来源分支
     */
    private String sourceBranch;

    /**
     * word文档路径
     */
    private String docxPath;

    /**
     * pdf文档路径
     */
    private String pdfPath;

    /**
     * 下载格式
     */
    private String type;

}
