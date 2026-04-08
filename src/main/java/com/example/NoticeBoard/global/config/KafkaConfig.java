package com.example.NoticeBoard.global.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

// Kafka Producer와 Consumer 설정 클래스
@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(){

        Map<String, Object> config = new HashMap<>();

        // kafka 시작 서버 주소(Broker 위치), 이벤트 key, value를 직렬화(파싱) 설정.
        // kafkaTemplate.send(TOPIC, postId) 구조
        // If the map previously contained a mapping for the key, the old value is replaced by the specified value.
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // TOPIC 부분
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // postId나 postId+":Like" 부분

        return new DefaultKafkaProducerFactory<>(config);
    }

    // kafka 이벤트(메세지) 전송
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }
}
