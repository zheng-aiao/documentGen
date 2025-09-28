package com.zaa.documentgen.service.redis;

import com.zaa.documentgen.model.GenewSwaggerHistoryInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GenewRedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String START_TIME_PREFIX = "genew:startTime:";
    private static final String ENCRYPT_PREFIX = "genew:encrypt:";
    private static final String DOCUMENT_PREFIX = "genew:document:";
    // 采用 Hash 结构统一存储服务状态，key 固定为 genew:status，field 为 serviceName
    private static final String STATUS_KEY = "genew:status";

    /**
     * 检查startTime是否存在于Redis集合中
     *
     * @param serviceName 服务名
     * @param startTime   启动时间
     * @return 是否存在
     */
    public boolean isStartTimeExists(String serviceName, String startTime) {
        try {
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            String key = START_TIME_PREFIX + serviceName;
            return Boolean.TRUE.equals(setOps.isMember(key, startTime));
        } catch (Exception e) {
            log.error("检查startTime失败, serviceName: {}, startTime: {}", serviceName, startTime, e);
            return false;
        }
    }

    /**
     * 检查encrypt值是否存在于Redis集合中
     *
     * @param serviceName 服务名
     * @param encrypt     加密值
     * @return 是否存在
     */
    public boolean isEncryptExists(String serviceName, String encrypt) {
        try {
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            String key = ENCRYPT_PREFIX + serviceName;
            return Boolean.TRUE.equals(setOps.isMember(key, encrypt));
        } catch (Exception e) {
            log.error("检查encrypt失败, serviceName: {}, encrypt: {}", serviceName, encrypt, e);
            return false;
        }
    }

    /**
     * 添加startTime到Redis集合
     *
     * @param serviceName 服务名
     * @param startTime   启动时间
     */
    public void addStartTime(String serviceName, String startTime) {
        try {
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            String key = START_TIME_PREFIX + serviceName;
            setOps.add(key, startTime);
            log.info("添加startTime成功, serviceName: {}, startTime: {}", serviceName, startTime);
        } catch (Exception e) {
            log.error("添加startTime失败, serviceName: {}, startTime: {}", serviceName, startTime, e);
        }
    }

    /**
     * 添加encrypt值到Redis集合
     *
     * @param serviceName 服务名
     * @param encrypt     加密值
     */
    public void addEncrypt(String serviceName, String encrypt) {
        try {
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            String key = ENCRYPT_PREFIX + serviceName;
            setOps.add(key, encrypt);
            log.info("添加encrypt成功, serviceName: {}, encrypt: {}", serviceName, encrypt);
        } catch (Exception e) {
            log.error("添加encrypt失败, serviceName: {}, encrypt: {}", serviceName, encrypt, e);
        }
    }

    /**
     * 保存文档信息到Redis
     *
     * @param serviceName  服务名
     * @param version      版本号
     * @param startTime    部署时间
     * @param sourceBranch 来源分支
     * @param docxPath 文档路径
     * @param pdfPath 文档路径
     */
    public void saveDocumentInfo(String serviceName, String version, String startTime,
            String sourceBranch, String docxPath, String pdfPath) {
        try {
            String key = DOCUMENT_PREFIX + serviceName;
            GenewSwaggerHistoryInfo info = GenewSwaggerHistoryInfo.builder().name(serviceName)
                    .version(version).startTime(startTime).sourceBranch(sourceBranch).docxPath(docxPath)
                    .pdfPath(pdfPath).build();
            redisTemplate.opsForHash().put(key,startTime,info);
            log.info("保存文档信息成功, serviceName: {}, info: {}", serviceName, info);
        } catch (Exception e) {
            log.error("保存文档信息失败, serviceName: {}", serviceName, e);
        }
    }

    /**
     * 获取文档信息
     *
     * @param serviceName 服务名
     * @return 文档信息
     */
    public List<GenewSwaggerHistoryInfo> getDocumentInfo(String serviceName) {
        try {
            String key = DOCUMENT_PREFIX + serviceName;
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries == null) {
                return List.of();
            }
            return entries.values().stream().map(GenewSwaggerHistoryInfo.class::cast).toList();
        } catch (Exception e) {
            log.error("获取文档信息失败, serviceName: {}", serviceName, e);
            return null;
        }
    }

    /**
     * 移除文档信息
     *
     * @param serviceName 服务名
     * @param startTime   启动时间
     */
    public void removeDocumentInfo(String serviceName, String startTime) {
        try {
            String key = DOCUMENT_PREFIX + serviceName;
            redisTemplate.opsForHash().delete(key, startTime);
        } catch (Exception e) {
            log.error("移除文档信息失败, serviceName: {}, version: {}", serviceName, startTime, e);
        }
    }

    /**
     * 批量设置服务状态，一次性写入 Redis Hash。
     *
     * @param statusMap key: serviceName, value: status
     */
    public void setServiceStatuses(Map<String, String> statusMap) {
        try {
            if (statusMap == null || statusMap.isEmpty()) {
                return;
            }
            redisTemplate.opsForHash().putAll(STATUS_KEY, statusMap);
            log.info("批量设置服务状态成功, size: {}", statusMap.size());
        } catch (Exception e) {
            log.error("批量设置服务状态失败", e);
        }
    }

    /**
     * 获取服务状态
     **
     * @return 状态文本，获取失败返回 null
     */
    public Map<Object, Object> getServiceStatus() {
        try {
            Map<Object, Object> allHashEntries = redisTemplate.opsForHash().entries(STATUS_KEY);
            return allHashEntries == null ? new HashMap<>() : allHashEntries;
        } catch (Exception e) {
            log.error("获取服务状态失败, {}", e);
            return null;
        }
    }

}
