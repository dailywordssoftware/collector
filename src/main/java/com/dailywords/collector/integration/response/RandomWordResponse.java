package com.dailywords.collector.integration.response;

import lombok.Data;

import java.util.List;

@Data
public class RandomWordResponse {
    private String word;
    private String frequency;

    private List<Result> results;
}
