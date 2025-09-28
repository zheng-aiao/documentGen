package com.zaa.documentgen.controller;

import com.zaa.documentgen.config.GenewConfig;
import com.zaa.documentgen.config.LicenseConfig;
import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.resp.R;
import com.zaa.documentgen.service.genservice.GenDocumentService;
import com.zaa.documentgen.service.redis.GenewRedisService;
import com.zaa.documentgen.util.DocumentPathUtils;
import com.zaa.documentgen.util.FileUtils;
import com.zaa.documentgen.util.httpUtils.HttpService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/doc")
@Slf4j
public class GenDocumentController {

    @Autowired
    HttpService httpCommonService;

    @Autowired
    GenDocumentService genDocumentService;

    @Autowired
    private GenewRedisService genewRedisService;

    @Autowired
    private GenewConfig genewConfig;

    @Operation(summary = "生成swagger文档, 仅支持swager2.0版本")
    @GetMapping("/generate")
    public R getSwaggerWordFile(@RequestParam("url") String url,
            @RequestParam("type") String type,
            @RequestParam("fileName") String fileName,
            @RequestParam("startTime") String startTime,
            @RequestParam(name="version", defaultValue = "dev") String version)
    {
        // HTTP请求，获得swagger的静态json文件
        LicenseConfig.setConfig();
        Map<String, Object> jsonMap = httpCommonService.loadJson(url,fileName+":"+version,startTime);

        // 生成文档
        fileName = fileName + "." + type;
        File file = FileUtils.createFile(DocxConf.genFilePath, fileName);
        boolean sign = genDocumentService.generateSwaggerFile(jsonMap, file, type);
        log.info("生成word接口文档完成 : 结果：" + (sign ? "success" : "failure"));
        return sign ? R.ok() : R.fail();
    }

    @Operation(summary = "生成并下载swagger文档")
    @GetMapping("/download")
    public ResponseEntity<?> downloadSwaggerFile(@RequestParam("url") String url,
            @RequestParam("type") String type,
            @RequestParam("fileName") String fileName,
            @RequestParam("startTime") String startTime){
        try {
            LicenseConfig.setConfig();
            Map<String, Object> jsonMap = httpCommonService.loadJson(url,fileName,startTime);
            Map<String, Object> info = (Map<String, Object>) jsonMap.get("info");
            String version = info != null? (info.get("version") == null ? "version_not_found" : info.get("version").toString()): "version_not_found"  ;
            String sourceBranch =info != null? ( info.get("sourceBranch") == null ? "" : info.get("sourceBranch").toString()): "";
            String normalizedType = "pdf".equalsIgnoreCase(type) ? "pdf" : "docx";


            // 创建文档目录
            String documentPath = DocumentPathUtils.getFullDocumentPath(
                    genewConfig.getDocumentOutputPath(), fileName, version, startTime, normalizedType);
            if (documentPath == null) {
                return ResponseEntity.internalServerError().body("文档生成失败");
            }
            File documentFile = new File(documentPath);
            boolean ok = genDocumentService.generateSwaggerFile(jsonMap, documentFile, normalizedType);
            if (!ok || !documentFile.exists() || documentFile.length() == 0) {
                if (documentFile.exists()) {
                    // 文件大小为0
                    documentFile.delete();
                    return ResponseEntity.internalServerError().body("文档生成失败");
                }
            }

            String docxPath = "";
            String pdfPath = "";
            if(normalizedType.equals("pdf")){
                pdfPath = documentPath;
            }else{
                docxPath = documentPath;
            }

            genewRedisService.saveDocumentInfo(fileName, version, startTime, sourceBranch, docxPath,pdfPath);
            String encoded = URLEncoder.encode(documentFile.getName(), StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            MediaType mediaType = "pdf".equalsIgnoreCase(normalizedType)
                    ? MediaType.APPLICATION_PDF
                    : MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .contentType(mediaType)
                    .contentLength(documentFile.length())
                    .body(new FileSystemResource(documentFile));
        } catch (Exception e) {
            log.error("download swagger file error", e);
            return ResponseEntity.internalServerError().body("服务端异常");
        }
    }

}
