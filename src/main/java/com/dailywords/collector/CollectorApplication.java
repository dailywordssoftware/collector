package com.dailywords.collector;

import com.dailywords.collector.domain.model.RandomWord;
import com.dailywords.collector.domain.task.FetchRandomWordsTask;
import com.dailywords.collector.integration.dto.TranslateDTO;
import com.dailywords.collector.integration.response.Result;
import com.dailywords.collector.repository.RandomWordRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;

@EnableMongoAuditing
@SpringBootApplication
public class CollectorApplication {
	private final FetchRandomWordsTask saveWordTask;
	private final WebClient webClient;

	public CollectorApplication(FetchRandomWordsTask saveWordTask, WebClient webClient) {
		this.saveWordTask = saveWordTask;
		this.webClient = webClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(CollectorApplication.class, args);
	}

	@Autowired
	private RandomWordRepository randomWordRepository;

	@Autowired
	private KafkaTemplate<String, RandomWord> kafkaTemplate;

	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> {
			RandomWord randomWord = new RandomWord();
			randomWord.setWord("Chronology");
			randomWord.setResults(List.of(new Result()));

			kafkaTemplate.send("orders", randomWord);
			System.out.println("Item has been sent.");
		};
	}
}
