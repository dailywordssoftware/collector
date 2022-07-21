package com.dailywords.collector.integration.kafka;

import com.dailywords.collector.domain.model.RandomWord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${com.daily-words.collector.bootstrapServers}")
    private String bootstrapServers;

    @Value("${spring.kafka.template.default-topic}")
    private String topic;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, RandomWordSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, RandomWord> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, RandomWord> kafkaTemplate() {
        KafkaTemplate<String, RandomWord> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic(topic);
        return template;
    }
}