package com.dailywords.collector.domain.model;

import com.dailywords.collector.integration.response.Result;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Data
@Document("random_word")
public class RandomWord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field
    private String word;

    @Field
    private List<Result> results;
}
