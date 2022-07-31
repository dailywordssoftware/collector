package com.dailywords.collector.batch.config;

import com.dailywords.collector.batch.listener.ItemCountListener;
import com.dailywords.collector.batch.processor.RandomWordItemFilteringProcessor;
import com.dailywords.collector.domain.model.RandomWord;
import com.dailywords.collector.integration.kafka.RandomWordKafkaConverter;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.quartz.*;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.kafka.KafkaItemWriter;
import org.springframework.batch.item.kafka.builder.KafkaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Configuration
@EnableBatchProcessing
@Log4j2
public class BatchConfig extends QuartzJobBean {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, RandomWord> kafkaTemplate;
    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;

    @Bean
    public ItemProcessor<RandomWord, RandomWord> randomWordItemFilteringProcessor() {
        return new RandomWordItemFilteringProcessor();
    }

    public Query itemReaderQuery() {
        Query query = new Query();
        query.cursorBatchSize(10);
        query.limit(1);
        query.with(Pageable.ofSize(20));
        return query;
    }

    @Bean
    public ItemReader<RandomWord> mongoItemReader() {
        Map<String, Sort.Direction> sortMap = new HashMap<>();
        sortMap.put("_id", Sort.Direction.DESC);

        // TODO: Read last number of items.
        return new MongoItemReaderBuilder<RandomWord>()
                .name("randomWordItemReader")
                .template(mongoTemplate)
                .query(itemReaderQuery())
                .sorts(sortMap)
                .currentItemCount(20)
                .pageSize(20)
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
    public ItemCountListener itemCountListener() {
        return new ItemCountListener();
    }

    @Bean
    public Step fetchRandomWordItemStep() {
        return stepBuilderFactory.get("fetchRandomWordItemStep")
                .<RandomWord, RandomWord>chunk(20)
                .reader(mongoItemReader())
                .processor(randomWordItemFilteringProcessor())
                .writer(kafkaItemWriter())
                .listener(itemCountListener())
                .build();
    }

    @Bean
    public Job randomDailyWordJob() {
        return jobBuilderFactory.get("randomDailyWordJob")
                .start(fetchRandomWordItemStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob(BatchConfig.class)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger trigger() {
        SimpleScheduleBuilder builder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInSeconds(10)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail())
                .withSchedule(builder)
                .build();
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobParameters parameters = new JobParametersBuilder(jobExplorer)
                .getNextJobParameters(randomDailyWordJob())
                .toJobParameters();

        try {
            this.jobLauncher.run(randomDailyWordJob(), parameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }
}
