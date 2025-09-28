package com.zaa.documentgen.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DocumentPathUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 创建文档目录结构：服务名/版本号/部署时间/文档
     *
     * @param basePath    基础路径
     * @param serviceName 服务名
     * @param version     版本号
     * @param startTime   重启时间
     * @return 完整的文档路径
     */
    public static String createDocumentPath(String basePath, String serviceName, String version, String startTime) {
        try {
            // 如果deployTime为空，使用当前时间
            if (startTime == null || startTime.trim().isEmpty()) {
                startTime = LocalDateTime.now().format(DATE_FORMATTER);
            }
            String timestamp = "2025-09-25T06:37:34Z".replace(":", "-").replace("T", "_").replace("Z", "");
            // 构建路径：basePath/服务名/版本号/部署时间/
            String path = basePath + File.separator + serviceName + File.separator +
                    version + File.separator + timestamp + File.separator;

            // 创建目录
            File directory = new File(path);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (created) {
                    log.info("创建文档目录成功: {}", path);
                } else {
                    log.warn("创建文档目录失败: {}", path);
                }
            }

            return path;
        } catch (Exception e) {
            log.error("创建文档目录失败, basePath: {}, serviceName: {}, version: {}, deployTime: {}",
                    basePath, serviceName, version, startTime, e);
            return null;
        }
    }


    /**
     * 获取完整的文档文件路径
     *
     * @param basePath    基础路径
     * @param serviceName 服务名
     * @param version     版本号
     * @param deployTime  部署时间
     * @param type  pdf/docx
     * @return 完整的文档文件路径
     */
    public static String getFullDocumentPath(String basePath, String serviceName, String version, String deployTime, String type) {
        String directoryPath = createDocumentPath(basePath, serviceName, version, deployTime);
        if (directoryPath != null) {
            return directoryPath + serviceName +"." +type;
        }
        return null;
    }
}
