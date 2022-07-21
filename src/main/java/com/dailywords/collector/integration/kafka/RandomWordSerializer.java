package com.dailywords.collector.integration.kafka;

import com.dailywords.collector.domain.model.RandomWord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class RandomWordSerializer implements Serializer<RandomWord> {
    @Override
    public byte[] serialize(String s, RandomWord randomWord) {
        try {
            return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(randomWord).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
