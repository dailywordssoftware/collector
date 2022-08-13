package com.dailywords.collector.batch.processor;

import com.dailywords.collector.domain.model.RandomWord;
import com.dailywords.collector.integration.wordsapi.WordsAPIClient;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemReader;

@AllArgsConstructor
public class RandomWordItemReader implements ItemReader<RandomWord> {
    private final WordsAPIClient client;

    @Override
    public RandomWord read() throws Exception {
        return client.getRandomWord()
                .map(word -> {
                    RandomWord randomWord = new RandomWord();
                    randomWord.setWord(word.getWord());
                    randomWord.setResults(word.getResults());
                    return randomWord;
                })
                .block();
    }
}
