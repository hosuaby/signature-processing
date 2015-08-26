package io.hosuaby.signatures.config;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;

import io.hosuaby.signatures.batch.GridFsScanWriter;
import io.hosuaby.signatures.batch.InboxScanCleaner;
import io.hosuaby.signatures.batch.InboxScanReader;
import io.hosuaby.signatures.batch.InboxTranscriptCleaner;
import io.hosuaby.signatures.batch.InboxTranscriptReader;
import io.hosuaby.signatures.batch.ListIdsLoader;
import io.hosuaby.signatures.batch.SignatureSignAdder;
import io.hosuaby.signatures.batch.TrascriptSignatureReader;
import io.hosuaby.signatures.domain.Signature;

/**
 * Spring Batch configuration.
 */
@Configuration
@EnableBatchProcessing
// TODO: check if steps must be declared as beans
// TODO: check the bean's javadoc
public class BatchConfig {

    /** Inbox directory */
    public static final String INBOX_DIR = "/tmp/signatures/inbox/";

    /** Builder factory for jobs */
    @Autowired
    private JobBuilderFactory jobs;

    /** Builder factory for steps */
    @Autowired
    private StepBuilderFactory steps;

    /** Mongo template */
    @Autowired
    private MongoOperations mongoTemplate;

    /**
     * @return main job
     */
    @Bean
    public Job job() {
        FlowBuilder<SimpleFlow> flows = new FlowBuilder<SimpleFlow>("fb");

        return jobs.get("mainJob")
                .incrementer(new RunIdIncrementer())
                .listener(listIdsLoader())
                .flow(loadScans())
                .split(new SimpleAsyncTaskExecutor())
                .add(flows
                        .from(loadTranscripts())
                        .end())
                .next(addSign())
                .end()
                .build();
    }

    /**
     * @return step loading image scans and saving them into GridFS
     */
    public Step loadScans() {
        return steps.get("loadScans")
                . <String, String> chunk(10)
                .reader(inboxScanReader())
                .writer(gridFsScanWriter())
                .listener(inboxScanCleaner())
                .build();
    }

    /**
     * @return step loading transcripts of signature lists
     */
    public Step loadTranscripts() {
        return steps.get("loadTranscripts")
                . <Signature, Signature> chunk(10)
                .reader(inboxTranscriptReader())
                .writer(signaturePreloadMongoWriter())
                .listener(inboxTrascriptCleaner())
                .build();
    }

    /**
     * @return step adding sign image to signature
     */
    public Step addSign() {
        return steps.get("addSign")
                . <Signature, Signature> chunk(10)
                .reader(signatureMongoReader())
                .processor(signatureSignAdder())
                .writer(signatureInboxMongoWriter())
                .build();
    }

    /**
     * @return store for ids of processed signature lists by the job.
     */
    @Bean(name = "scanStore")
    public Map<String, BufferedImage> scanStore() {
        return Collections.synchronizedMap(
                new HashMap<String, BufferedImage>());
    }

    @Bean
    public JobExecutionListener listIdsLoader() {
        return new ListIdsLoader();
    }

    /**
     * @return reader of images from infox directory
     */
    @Bean
    public ItemReader<String> inboxScanReader() {
        return new InboxScanReader();
    }

    /**
     * @return writer for images in Grid FS
     */
    @Bean
    public ItemWriter<String> gridFsScanWriter() {
        return new GridFsScanWriter();
    }

    /**
     * @return cleaner of loaded scan images
     */
    @Bean
    public InboxScanCleaner inboxScanCleaner() {
        return new InboxScanCleaner();
    }

    /**
     * @return reader of files from inbox folder
     */
    @Bean
    public ItemReader<Signature> inboxTranscriptReader() {
        InboxTranscriptReader transcriptsReader = new InboxTranscriptReader();
        transcriptsReader.setDelegate(trascriptSignatureReader());
        return transcriptsReader;
    }

    /**
     * @return reader of {@code Signature}  from trascript text file
     */
    @Bean
    public FlatFileItemReader<Signature> trascriptSignatureReader() {
        return new TrascriptSignatureReader();
    }

    /**
     * @return writer of {@link Signature}s to mongo
     */
    @Bean
    public ItemWriter<Signature> signaturePreloadMongoWriter() {
        MongoItemWriter<Signature> itemWriter = new MongoItemWriter<>();
        itemWriter.setTemplate(mongoTemplate);
        itemWriter.setCollection("preload");
        return itemWriter;
    }

    /**
     * @return adder of sign to signature
     */
    @Bean
    public ItemProcessor<Signature, Signature> signatureSignAdder() {
        return new SignatureSignAdder();
    }

    /**
     * @return reader of {@code Signature}s from collection "inbox" in mongo
     */
    // TODO: create special reader
    @Bean
    public ItemReader<Signature> signatureMongoReader() {
        MongoItemReader<Signature> itemReader = new MongoItemReader<>();
        itemReader.setTemplate(mongoTemplate);
        itemReader.setCollection("preload");
        itemReader.setTargetType(Signature.class);
        itemReader.setQuery("{}");
        itemReader.setSort(new HashMap<String, Direction>() {
            private static final long serialVersionUID = 1L;
            {
                put("listId", Direction.ASC);
                put("lineNumber", Direction.ASC);
            }
        });
        return itemReader;
    }

    @Bean
    public ItemWriter<Signature> signatureInboxMongoWriter() {
        MongoItemWriter<Signature> itemWriter = new MongoItemWriter<>();
        itemWriter.setTemplate(mongoTemplate);
        itemWriter.setCollection("inbox");
        return itemWriter;
    }

    /**
     * @return cleaner of loaded scan images
     */
    @Bean
    public InboxTranscriptCleaner inboxTrascriptCleaner() {
        return new InboxTranscriptCleaner();
    }

}
