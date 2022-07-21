package com.dailywords.collector.batch.reader;

import com.dailywords.collector.domain.model.RandomWord;
import com.dailywords.collector.integration.wordsapi.WordsAPIClient;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemReader;

@AllArgsConstructor
public class RandomWordItemReader implements ItemReader<RandomWord> {

    private final WordsAPIClient wordsClient;

    @Override
    public RandomWord read() {
        return wordsClient.getRandomWord().block();
    }
}
