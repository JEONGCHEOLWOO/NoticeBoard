package com.example.NoticeBoard.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// Redis 연결과 직렬화를 설정하는 클래스
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 이 템플릿을 Redis 서버에 연결 - localhost:6379(default)
        // Sets the factory used to acquire connections and perform operations on the connected Redis instance.
        template.setConnectionFactory(connectionFactory);

        // 이 템플릿에서 사용할 키와 값을 직렬화(파싱) (키 - String , 값 - Json 으로 저장.)
        // Sets the key serializer to be used by this template.
        // Sets the value serializer to be used by this template.
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return template;
    }
}
