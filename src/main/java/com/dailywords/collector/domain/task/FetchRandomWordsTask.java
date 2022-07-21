package com.dailywords.collector.domain.task;

import com.dailywords.collector.domain.model.RandomWord;
import com.dailywords.collector.integration.wordsapi.GoogleTranslateAPIClient;
import com.dailywords.collector.integration.wordsapi.WordsAPIClient;
import com.dailywords.collector.repository.RandomWordRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Data
@Component
@AllArgsConstructor
public class FetchRandomWordsTask {
    private final WordsAPIClient wordsClient;
    private final GoogleTranslateAPIClient translateClient;
    private final RandomWordRepository randomWordRepository;

    @Scheduled(cron = "${tasks.fetch-random-words}")
    public void fetchRandomWords() {
        log.info("Fetch random words");
        wordsClient.getRandomWords()
                .filter(word -> Objects.nonNull(word.getResults()))
                .subscribe(word -> {
                    RandomWord randomWord = new RandomWord();
                    randomWord.setWord(word.getWord());
                    randomWord.setResults(word.getResults());
                    randomWordRepository.save(randomWord);
                });
    }
}
