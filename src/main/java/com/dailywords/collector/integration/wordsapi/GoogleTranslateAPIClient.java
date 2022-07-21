package com.dailywords.collector.integration.wordsapi;

import com.dailywords.collector.integration.dto.TranslateDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Slf4j
@AllArgsConstructor
public class GoogleTranslateAPIClient {
    private WebClient webClient;


    public String translateV2(String word) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://google-translate1.p.rapidapi.com/language/translate/v2"))
                .header("content-type", "application/x-www-form-urlencoded")
                .header("Accept-Encoding", "application/gzip")
                .header("X-RapidAPI-Key", "fcef75da80msh9d2e6c33c19bec5p16f77bjsnbca09f37d60e")
                .header("X-RapidAPI-Host", "google-translate1.p.rapidapi.com")
                .method("POST", HttpRequest.BodyPublishers.ofString("q=Hello%2C%20world!&target=es&source=en"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public Mono<String> translateWord(String word) {
        TranslateDTO request = new TranslateDTO();
        request.setQ(word);
        request.setSource("tr");
        request.setTarget("en");

        return webClient.post().uri("https://google-translate1.p.rapidapi.com/language/translate/v2")
//                .header("content-type", "application/x-www-form-urlencoded")
                .header("Accept-Encoding", "application/gzip")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("X-RapidAPI-Key", "fcef75da80msh9d2e6c33c19bec5p16f77bjsnbca09f37d60e")
                .header("X-RapidAPI-Host", "google-translate1.p.rapidapi.com")
                .body(Mono.just(request), String.class)
                .retrieve()
                .bodyToMono(String.class);
    }
}
