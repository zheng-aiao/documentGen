package com.zaa.documentgen.service.genservice;

import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.gen.AsposeDocGenerator;
import com.zaa.documentgen.model.ModelAttr;
import com.zaa.documentgen.parse.Swagger3JsonProcessor;
import com.zaa.documentgen.parse.SwaggerJsonProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GenDocumentService {

    public boolean generateSwaggerFile(Map<String, Object> jsonMap, File desFile, String type) {
        try {
            log.info("======================文档生成开始==========================");


            boolean isV3 = jsonMap.containsKey("openapi");

            Map<String, String> mapIndex = isV3
                    ? Swagger3JsonProcessor.parseDocHome(jsonMap)
                    : SwaggerJsonProcessor.parseDocHome(jsonMap);
            log.info("解析首页数据完成");

            Map<String, ModelAttr> definitinMap = isV3
                    ? Swagger3JsonProcessor.parseDefinitions(jsonMap)
                    : SwaggerJsonProcessor.parseDefinitions(jsonMap);
            log.info("解析ModelAttr复杂类型数据完成");

            Map<String, Object> tagsMap = isV3
                    ? Swagger3JsonProcessor.parseTags(jsonMap)
                    : SwaggerJsonProcessor.parseTags(jsonMap);
            log.info("解析tags分组信息完成");

            Map<String, List<Map<String, Object>>> pathMapAll = isV3
                    ? Swagger3JsonProcessor.parsePaths(jsonMap, tagsMap, definitinMap)
                    : SwaggerJsonProcessor.parsePaths(jsonMap, tagsMap, definitinMap);
            log.info("解析Paths接口请求数据完成");

            // 6.生成word
            AsposeDocGenerator.generateSwaggerFile(DocxConf.getInstance(), pathMapAll, mapIndex, desFile, type);
            log.info("======================文档生成结束==========================");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void docxToPdf(String docxPath, File pdfFile) {

        AsposeDocGenerator.docxToPdf(docxPath, pdfFile);
    }
}
