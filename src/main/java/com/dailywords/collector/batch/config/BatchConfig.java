package com.dailywords.collector.batch.config;

import com.dailywords.collector.batch.processor.RandomWordItemFilteringProcessor;
import com.dailywords.collector.domain.model.RandomWord;
import com.dailywords.collector.integration.kafka.RandomWordKafkaConverter;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.kafka.KafkaItemWriter;
import org.springframework.batch.item.kafka.builder.KafkaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;

@AllArgsConstructor
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, RandomWord> kafkaTemplate;

    @Bean
    public ItemProcessor<RandomWord, RandomWord> randomWordItemFilteringProcessor() {
        return new RandomWordItemFilteringProcessor();
    }

    @Bean
    public ItemReader<RandomWord> mongoItemReader() {
        return new MongoItemReaderBuilder<RandomWord>()
                .name("randomWordItemReader")
                .template(mongoTemplate)
                .jsonQuery("db.collector.findOne()")
                .sorts(new HashMap<>(100))
                .pageSize(10)
                .targetType(RandomWord.class)
                .build();
    }

    @Bean
    public Converter<RandomWord, String> converter() {
        return new RandomWordKafkaConverter();
    }

    @Bean
    public KafkaItemWriter<String, RandomWord> kafkaItemWriter() {
        return new KafkaItemWriterBuilder<String, RandomWord>()
                .kafkaTemplate(kafkaTemplate)
                .itemKeyMapper(converter())
                .build();
    }

    @Bean
    public ItemWriter<RandomWord> jsonFileItemWriter() {
        return new JsonFileItemWriterBuilder<RandomWord>()
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource("data/random_words.json"))
                .name("jsonFileItemWriter")
                .build();
    }

    @Bean
    public Step fetchRandomWordItemStep() {
        return stepBuilderFactory.get("fetchRandomWordItemStep")
                .<RandomWord, RandomWord>chunk(50)
                .reader(mongoItemReader())
//                .processor(randomWordItemFilteringProcessor())
                .writer(kafkaItemWriter())
                .build();
    }

    @Bean
    public Job randomDailyWordJob() {
        return jobBuilderFactory.get("randomDailyWordJob")
                .start(fetchRandomWordItemStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }
}
