package com.zaa.documentgen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${redis.key-prefix:docgen:}")
    private String redisKeyPrefix;

    // 交给 Spring Boot 自动装配 RedisConnectionFactory，避免生命周期不一致

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new PrefixStringRedisSerializer(redisKeyPrefix);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Adds a fixed prefix to all Redis keys and hash keys written via
     * RedisTemplate.
     */
    static class PrefixStringRedisSerializer extends StringRedisSerializer {
        private final String prefix;

        PrefixStringRedisSerializer(String prefix) {
            this.prefix = prefix == null ? "" : prefix;
        }

        @Override
        public byte[] serialize(@Nullable String string) {
            if (string == null) {
                return super.serialize(null);
            }
            String withPrefix = string.startsWith(prefix) ? string : prefix + string;
            return super.serialize(withPrefix);
        }

        @Override
        public String deserialize(@Nullable byte[] bytes) {
            String value = super.deserialize(bytes);
            if (value == null) {
                return null;
            }
            return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
        }
    }
}
