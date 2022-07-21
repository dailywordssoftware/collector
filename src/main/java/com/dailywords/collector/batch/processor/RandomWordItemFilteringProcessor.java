package com.dailywords.collector.batch.processor;

import com.dailywords.collector.domain.model.RandomWord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.util.Objects;

@Slf4j
public class RandomWordItemFilteringProcessor implements ItemProcessor<RandomWord, RandomWord>{

    @Override
    public RandomWord process(RandomWord randomWord) {
        System.out.println("Filtering random word");
        return Objects.nonNull(randomWord.getResults()) ? randomWord : null;
    }
}
