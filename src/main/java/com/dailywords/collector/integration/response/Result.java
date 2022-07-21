package com.dailywords.collector.integration.response;

import lombok.Data;

@Data
public class Result {
    private String definition;
    private String partOfSpeech;
    private String[] synonyms;
    private String[] typeOf;
    private String[] memberOf;
}
