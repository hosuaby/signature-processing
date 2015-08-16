package io.hosuaby.signatures.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Spring Batch configuration.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    /** Builder factory for jobs */
    @Autowired
    private JobBuilderFactory jobs;

    /** Builder factory for steps */
    @Autowired
    private StepBuilderFactory steps;

    /** Mongo template */
    @Autowired
    private MongoOperations template;

}
