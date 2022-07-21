package com.dailywords.collector.repository;

import com.dailywords.collector.domain.model.RandomWord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomWordRepository extends MongoRepository<RandomWord, String> {
    Boolean existsByWord(String word);
}
