package com.dailywords.collector.integration.kafka;

import com.dailywords.collector.domain.model.RandomWord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;

public class RandomWordKafkaConverter implements Converter<RandomWord, String> {

    @Override
    public String convert(RandomWord source) {
        try {
            return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
