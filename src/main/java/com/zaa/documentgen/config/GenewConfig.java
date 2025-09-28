package com.zaa.documentgen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "genew")
public class GenewConfig {

    /**
     * Genew文档服务器地址
     */
    private String serverUrl;

    /**
     * 文档输出路径
     */
    private String documentOutputPath;
}
