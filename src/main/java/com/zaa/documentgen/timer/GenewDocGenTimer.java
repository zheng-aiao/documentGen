package com.zaa.documentgen.timer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaa.documentgen.config.GenewConfig;
import com.zaa.documentgen.service.genservice.GenDocumentService;
import com.zaa.documentgen.service.redis.GenewRedisService;
import com.zaa.documentgen.util.DocumentPathUtils;
import com.zaa.documentgen.util.EncryptUtils;
import com.zaa.documentgen.util.httpUtils.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GenewDocGenTimer {

    @Autowired
    private GenewConfig genewConfig;

    @Autowired
    private HttpService httpService;

    @Autowired
    private GenewRedisService genewRedisService;

    @Autowired
    private GenDocumentService genDocumentService;

    @Scheduled(initialDelay = 5000, fixedRate = 300000)
    public void genGenewDocument() {
        log.info("=================================开始执行Genew文档生成定时任务===============================");
        long startTime = System.currentTimeMillis();
        try {
            // 1. 获取genew所有swagger文档信息
            String genewSwaggerServiceUrl = genewConfig.getServerUrl();
            String swaggerListJson = httpService.getData(genewSwaggerServiceUrl);

            if (swaggerListJson == null || swaggerListJson.trim().isEmpty()) {
                log.warn("获取genew  swagger 服务接口列表失败，URL: {}", genewSwaggerServiceUrl);
                return;
            }

            // 解析JSON数组
            JSONArray swaggerArray = JSON.parseArray(swaggerListJson);
            if (swaggerArray == null || swaggerArray.isEmpty()) {
                log.info("没有找到swagger文档信息");
                return;
            }

            log.info("获取到{}个swagger文档", swaggerArray.size());

            // 收集一次任务中的服务状态，减少多次写 Redis
            Map<String, String> statusBatch = new ConcurrentHashMap<>();

            // 2. 遍历数组处理每个服务
            for (int i = 0; i < swaggerArray.size(); i++) {
                JSONObject swaggerItem = swaggerArray.getJSONObject(i);
                processSwaggerDocument(swaggerItem, i + 1, statusBatch);
            }

            // 批量落库服务状态
            genewRedisService.setServiceStatuses(statusBatch);

            log.info("Genew文档生成定时任务执行完成");

        } catch (Exception e) {
            log.error("执行Genew文档生成定时任务失败", e);
        }
        long endTime = System.currentTimeMillis();
        log.info("================= 执行Genew文档生成定时任务 结束， 耗时：{} ms ====================", endTime - startTime);

    }

    /**
     * 处理单个swagger文档
     */
    private void processSwaggerDocument(JSONObject swaggerItem, int index, Map<String, String> statusBatch) {
        try {
            // 获取document信息
            JSONObject document = swaggerItem.getJSONObject("document");
            if (document == null) {
                log.warn("文档信息为空，跳过处理");
                return;
            }

            String serviceName = document.getString("name");
            String url = document.getString("url");
            String startTime = document.getString("startTime");

            if (serviceName == null || url == null || startTime == null) {
                log.warn("服务信息不完整，跳过处理: serviceName={}, url={}, startTime={}",
                        serviceName, url, startTime);
                return;
            }

            log.info("{}.处理服务: {}, URL: {}, startTime: {}", index, serviceName, url, startTime);

            // 3. 第一重判断：检查startTime是否已处理
            if (genewRedisService.isStartTimeExists(serviceName, startTime)) {
                log.info("服务 {} 的startTime {} 已处理过，跳过", serviceName, startTime);
                return;
            }

            // 获取JSON数据并计算encrypt值
            String swaggerJson = httpService.getData(url);
            if (swaggerJson == null || swaggerJson.trim().isEmpty()) {
                log.warn("获取swagger JSON数据失败，URL: {}", url);
                statusBatch.put(serviceName, "异常");
                return;
            }
            // 如果接口返回的是HTML（如错误页），则跳过处理，避免JSON解析报错
            String trimmedResponse = swaggerJson.trim();
            if (trimmedResponse.startsWith("<")
                    || trimmedResponse.toLowerCase().contains("<!doctype html")
                    || trimmedResponse.toLowerCase().contains("<html")) {
                log.warn("获取到的swagger响应疑似HTML内容，跳过该服务。URL: {}", url);
                statusBatch.put(serviceName, "异常");
                return;
            }

            // 计算encrypt值
            String encrypt = EncryptUtils.sha256(swaggerJson);
            if (encrypt == null) {
                log.warn("计算encrypt值失败，跳过处理");
                return;
            }

            // 4. 第二重判断： 检查encrypt是否已处理
            if (genewRedisService.isEncryptExists(serviceName, encrypt)) {
                log.info("服务 {} 的encrypt值已处理过，跳过", serviceName);
                return;
            }

            // 5. 生成word文档
            generateDocument(serviceName, swaggerJson, startTime, encrypt, statusBatch);

        } catch (Exception e) {
            log.error("处理swagger文档失败", e);
        }
    }

    /**
     * 生成文档
     */
    private void generateDocument(String serviceName, String swaggerJson, String startTime, String encrypt,
            Map<String, String> statusBatch) {
        // 生成前默认标记状态为“异常”，生成成功后再置为“正常”。只收集到批量 Map，不立即写 Redis。
        statusBatch.put(serviceName, "异常");
        try {
            // 解析swagger JSON
            ObjectMapper om = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = om.readValue(swaggerJson, HashMap.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) jsonMap.get("info");

            // 异常json数 ep: {"detail":"Not Found"}
            if (info == null) {
                return;
            }

            String version = info.get("version") == null ? "version_not_found" : info.get("version").toString();
            String sourceBranch = info.get("sourceBranch") == null ? "" : info.get("sourceBranch").toString();

            // 创建文档目录
            String documentPath = DocumentPathUtils.getFullDocumentPath(
                    genewConfig.getDocumentOutputPath(), serviceName, version, startTime, "docx");

            if (documentPath == null) {
                log.error("创建文档目录失败，跳过生成");
                return;
            }

            File documentFile = new File(documentPath);

            // 生成word文档
            boolean success = genDocumentService.generateSwaggerFile(jsonMap, documentFile, "docx");

            if (success) {
                log.info("文档生成成功: {}", documentPath);

                // 保存相关信息到Redis
                genewRedisService.addStartTime(serviceName, startTime);
                genewRedisService.addEncrypt(serviceName, encrypt);

                // 默认只生成word文档，pdf暂时不生成
                genewRedisService.saveDocumentInfo(serviceName, version, startTime, sourceBranch, documentPath,"");

                // 标记状态为正常（写入批量集合）
                statusBatch.put(serviceName, "正常");

                log.info("服务 {} 的文档生成完成，相关信息已保存到Redis", serviceName);
            } else {
                log.error("文档生成失败: {}", documentPath);
            }

        } catch (Exception e) {
            log.error("生成文档失败, serviceName: {}", serviceName, e);
        }
    }
}
