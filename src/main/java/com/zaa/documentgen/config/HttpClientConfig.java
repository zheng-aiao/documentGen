package com.zaa.documentgen.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class HttpClientConfig {

        @Bean
        public CloseableHttpClient httpClient()
                        throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
                // 1. 创建 SSL 上下文（信任所有证书）
                SSLContext sslContext = SSLContexts.custom()
                                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                                .build();

                // 2. 创建忽略主机名和证书验证的 SocketFactory，并注册到连接管理器
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                                sslContext,
                                null,
                                null,
                                NoopHostnameVerifier.INSTANCE);

                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                                .<ConnectionSocketFactory>create()
                                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                                .register("https", sslSocketFactory)
                                .build();

                // 3. 配置连接池
                PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                cm.setMaxTotal(200);
                cm.setDefaultMaxPerRoute(50);

                // 4. 配置超时
                RequestConfig rc = RequestConfig.custom()
                                .setConnectTimeout(30000)
                                .setSocketTimeout(30000)
                                .setConnectionRequestTimeout(30000)
                                .build();

                // 5. 构建 HttpClient
                return HttpClients.custom()
                                .setSSLSocketFactory(sslSocketFactory) // 关键：忽略证书验证
                                .setConnectionManager(cm)
                                .setDefaultRequestConfig(rc)
                                .evictExpiredConnections()
                                .build();
        }
}
