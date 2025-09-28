package com.zaa.documentgen.controller;

import com.zaa.documentgen.model.GenewSwaggerHistoryInfo;
import com.zaa.documentgen.resp.R;
import com.zaa.documentgen.resp.ResultCodeEnum;
import com.zaa.documentgen.service.genservice.GenDocumentService;
import com.zaa.documentgen.service.redis.GenewRedisService;
import com.zaa.documentgen.util.httpUtils.HttpService;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/doc/genew")
@Slf4j
public class GenewController {

    @Autowired
    HttpService httpCommonService;

    @Autowired
    private GenewRedisService genewRedisService;

    @Autowired
    GenDocumentService genDocumentService;

    @Operation(summary = "导入配置", description = "获取genew文档服务集成的所有swagger接口信息")
    @GetMapping("/import/config")
    public R<String> getSwaggerWordFile(@RequestParam("url") String swaggerUrls,
            @RequestParam("fileName") String fileName) {

        String result = httpCommonService.getData(swaggerUrls);
        if (StringUtils.isEmpty(result)) {
            return R.result(ResultCodeEnum.RESULT_FAIL_GET_SWAGGER_JSON);
        }

        return R.ok();
    }

    @Operation(summary = "历史版本", description = "获取genew文档服务集成的所有swagger接口信息")
    @GetMapping("/history")
    public R<List<GenewSwaggerHistoryInfo>> getServiceHistoryVersion(@RequestParam("serviceName") String serviceName,
    @RequestParam(value = "refresh", defaultValue = "false") Boolean refresh) {

        List<GenewSwaggerHistoryInfo> historys = genewRedisService.getDocumentInfo(serviceName);





        if(refresh && historys != null && !historys.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    List<GenewSwaggerHistoryInfo> toRemove = new ArrayList<>();

                    for (GenewSwaggerHistoryInfo info : historys) {
                        String docxPath = info.getDocxPath();
                        if (docxPath != null && !docxPath.trim().isEmpty()) {
                            File file = new File(docxPath);
                            if (!file.exists() || file.length() == 0) {
                                log.warn("发现不存在的文件，准备清理Redis记录: serviceName={}, version={}, documentPath={}",
                                        info.getName(), info.getVersion(), docxPath);
                                toRemove.add(info);
                            }
                        }
                    }

                    // 批量清理不存在的文件记录
                    for (GenewSwaggerHistoryInfo info : toRemove) {
                        genewRedisService.removeDocumentInfo(info.getName(), info.getStartTime());
                        log.info("已清理不存在的文件记录: serviceName={}, version={}, startTime={}",
                                info.getName(), info.getVersion(), info.getStartTime());
                    }

                } catch (Exception e) {
                    log.error("异步清理文件记录失败", e);
                }
            });
        }
        return R.data(historys);
    }

    @Operation(summary = "获取服务状态", description = "获取服务状态")
    @GetMapping("/status")
    public R<Map<Object, Object>> getServiceStatus() {
        Map<Object, Object> status = genewRedisService.getServiceStatus();
        return R.data(status);
    }

    @Operation(summary = "历史版本下载", description = "历史版本下载")
    @PostMapping("/history/download")
    public ResponseEntity<?> downloadSwaggerHistoryFile(@RequestBody GenewSwaggerHistoryInfo genewSwaggerHistoryInfo) {
        try {
            String documentPath = genewSwaggerHistoryInfo.getType().equals("docx") ? genewSwaggerHistoryInfo.getDocxPath() : genewSwaggerHistoryInfo.getPdfPath();

            // 服务器发现这个文件不存在，则删除对应的redis记录，不能在生成（因为生成的版本不对应）
            File targetFile = new File(documentPath);
            if (!targetFile.exists() || targetFile.length() == 0) {

                // 存在pdf 不存在word的情况， 直接删除redis记录，刷新界面
                if(genewSwaggerHistoryInfo.getType().equals("docx")){
                    genewRedisService.removeDocumentInfo(genewSwaggerHistoryInfo.getName(),
                            genewSwaggerHistoryInfo.getStartTime());
                    return ResponseEntity.status(404).body("FILE_NOT_FOUND");
                }

                // 存在word 不存在pdf的情况， 直接docx转pdf
                if(genewSwaggerHistoryInfo.getType().equals("pdf")){
                    String docxPath = genewSwaggerHistoryInfo.getDocxPath();
                    if(StringUtils.isEmpty(docxPath)){
                        return ResponseEntity.status(404).body("FILE_NOT_FOUND");
                    }else{
                        String pdfPath = docxPath.replace("docx", "pdf");
                        targetFile = new File(pdfPath);
                        genDocumentService.docxToPdf(docxPath, targetFile);

                    }
                }

            }

            // 下载返回
            String fileName = targetFile.getName();
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            MediaType mediaType = fileName.toLowerCase().endsWith(".pdf")
                    ? MediaType.APPLICATION_PDF
                    : MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .contentType(mediaType)
                    .contentLength(targetFile.length())
                    .body(new FileSystemResource(targetFile));
        } catch (Exception e) {
            log.error("history download error", e);
            return ResponseEntity.internalServerError().body("服务端异常");
        }
    }

}
