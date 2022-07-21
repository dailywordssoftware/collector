package com.dailywords.collector.integration.wordsapi;

import com.dailywords.collector.domain.model.RandomWord;
import com.dailywords.collector.integration.dto.TranslateDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@AllArgsConstructor
public class WordsAPIClient {
    private WebClient webClient;
    private GoogleTranslateAPIClient translateAPIClient;

    public ParallelFlux<RandomWord> getRandomWords() {
        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("adf");

        Scheduler scheduler = Schedulers.newBoundedElastic(5, 10 , "getRandomWordsThread");

        return Flux.range(1, 20)
                .parallel()
                .log()
                .flatMap(i -> webClient.get()
                        .uri("https://wordsapiv1.p.rapidapi.com/words/?random=true")
                        .header("X-RapidAPI-Key", "fcef75da80msh9d2e6c33c19bec5p16f77bjsnbca09f37d60e")
                        .header("X-RapidAPI-Host", "wordsapiv1.p.rapidapi.com")
                        .retrieve()
                        .bodyToMono(RandomWord.class).publishOn(scheduler));
    }

    public Mono<RandomWord> getRandomWord() {
        return webClient.get()
                .uri("https://wordsapiv1.p.rapidapi.com/words/?random=true")
                .header("X-RapidAPI-Key", "fcef75da80msh9d2e6c33c19bec5p16f77bjsnbca09f37d60e")
                .header("X-RapidAPI-Host", "wordsapiv1.p.rapidapi.com")
                .retrieve()
                .bodyToMono(RandomWord.class);
    }


    public Flux<RandomWord> getRandomWorldWithTranslate() {
        Flux.range(1, 20)
                .log()
                .flatMap(i -> webClient.get()
                        .uri("https://wordsapiv1.p.rapidapi.com/words/?random=true")
                        .header("X-RapidAPI-Key", "fcef75da80msh9d2e6c33c19bec5p16f77bjsnbca09f37d60e")
                        .header("X-RapidAPI-Host", "wordsapiv1.p.rapidapi.com")
                        .retrieve()
                        .bodyToMono(RandomWord.class)
                        .flatMap(randomWord -> {
                            TranslateDTO request = new TranslateDTO();
                            request.setQ(randomWord.getWord());
                            request.setSource("tr");
                            request.setTarget("en");
                            return webClient.post().uri("https://google-translate1.p.rapidapi.com/language/translate/v2")
                                    .header("Accept-Encoding", "application/gzip")
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .header("X-RapidAPI-Key", "fcef75da80msh9d2e6c33c19bec5p16f77bjsnbca09f37d60e")
                                    .header("X-RapidAPI-Host", "google-translate1.p.rapidapi.com")
                                    .body(Mono.just(request), String.class)
                                    .retrieve()
                                    .bodyToMono(String.class);
                        })
                );

        return null;
    }
}
