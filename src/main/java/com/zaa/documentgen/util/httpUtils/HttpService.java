package com.zaa.documentgen.util.httpUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaa.documentgen.exception.BaseException;
import com.zaa.documentgen.resp.ResultCodeEnum;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class HttpService {

    private final CloseableHttpClient httpClient;

    @Autowired
    RedisTemplate redisTemplate;

    public HttpService(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 该方法通过指定的url。模拟HTTP请求返回swagger接口文档的json信息
     *
     * @param url
     * @return
     */

    public String getData (String url) {
        HttpGet get = new HttpGet(url);
        try (CloseableHttpResponse resp = httpClient.execute(get)) {
            if (resp.getStatusLine().getStatusCode() != 200) {
                log.error("获取 swagger json 失败, url={}, status code={}", url, resp.getStatusLine().getStatusCode());
                return null;
            }
            HttpEntity entity = resp.getEntity();
            return entity == null ? null : EntityUtils.toString(entity);
        } catch (Exception e) {
            log.error("获取 swagger json 失败, url={}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 获取swagger json 数据， redis缓存优化， Genew服务重新启动则重新获取
     *
     * @param url
     * @param key 保证是 同一个服务的同一个版本
     * @param startTime 保证启动时间一致
     * @return
     */
    public Map<String, Object> loadJson(String url,String key,String startTime) {

        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

        if (valueOps.get(key)!= null) {
            String preStartTime1 = ((Map) valueOps.get(key)).get("startTime").toString();
            if(startTime.equals(preStartTime1)){
                log.info("loadJson type: redis cache, key:{},",key);
                return (Map) valueOps.get(key);
            }
        }
        Map<String, Object> jsonMap = new HashMap<>();
        try {
            log.info("loadJson type: http request, url:{},",url);
            String result = getData(url);
            if (StringUtils.isEmpty(result)) {
                throw new BaseException(ResultCodeEnum.RESULT_FAIL_GET_SWAGGER_JSON);
            }
            ObjectMapper om = new ObjectMapper();
            jsonMap = om.readValue(result, HashMap.class);
            jsonMap.put("startTime", startTime);
            valueOps.set(key, jsonMap);
        } catch (JsonProcessingException e) {
            throw new BaseException(ResultCodeEnum.RESULT_FAIL_GET_SWAGGER_JSON);
        }
        return jsonMap;
    }
}
