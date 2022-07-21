package com.dailywords.collector.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class TranslateDTO {
    private String q;
    private String source;
    private String target;
}
