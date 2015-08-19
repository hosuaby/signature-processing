package io.hosuaby.signatures.config;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import io.hosuaby.signatures.batch.GridFsScanWriter;
import io.hosuaby.signatures.batch.InboxScanCleaner;
import io.hosuaby.signatures.batch.InboxScanReader;
import io.hosuaby.signatures.batch.InboxTranscriptCleaner;
import io.hosuaby.signatures.batch.InboxTranscriptReader;
import io.hosuaby.signatures.batch.ListIdsLoader;
import io.hosuaby.signatures.batch.TrascriptSignatureReader;
import io.hosuaby.signatures.domain.Signature;

/**
 * Spring Batch configuration.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    /** Inbox directory */
    private static final String INBOX_DIR = "/tmp/signatures/inbox/";

    /** Builder factory for jobs */
    @Autowired
    private JobBuilderFactory jobs;

    /** Builder factory for steps */
    @Autowired
    private StepBuilderFactory steps;

    /** Mongo template */
    @Autowired
    private MongoOperations mongoTemplate;

    /** GridFS template */
    @Autowired
    private GridFsTemplate gridFsTemplate;

    /** Resource loader */
    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * @return main job
     */
    @Bean
    public Job job() {
        return jobs.get("mainJob")
                .incrementer(new RunIdIncrementer())
                .listener(listIdsLoader())
                .flow(loadScans())
                .next(loadTranscripts())
                .end()
                .build();
    }

    /**
     * @return loads the image scans and saves them into GridFS
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
     * @return loads transcripts of signature lists
     */
    public Step loadTranscripts() {
        return steps.get("loadTranscripts")
                .<Signature, Signature> chunk(10)
                .reader(inboxTranscriptReader())
                .writer(signatureMongoWriter())
                .listener(inboxTrascriptCleaner())
                .build();
    }

    /**
     * @return store for ids of processed signature lists by the job.
     */
    @Bean
    @JobScope
    public Map<String, BufferedImage> scanStore() {
        return new HashMap<String, BufferedImage>();
    }

    @Bean
    public JobExecutionListener listIdsLoader() {
        ListIdsLoader loader =
                new ListIdsLoader();
        loader.setBaseDir(INBOX_DIR);
        loader.setScanStore(scanStore());
        return loader;
    }

    /**
     * @return reader of images from infox directory
     */
    @Bean
    public ItemReader<String> inboxScanReader() {
        InboxScanReader scanReader = new InboxScanReader();
        scanReader.setBaseDir(INBOX_DIR);
        scanReader.setScanStore(scanStore());
        return scanReader;
    }

    /**
     * @return writer for images in Grid FS
     */
    @Bean
    public ItemWriter<String> gridFsScanWriter() {
        GridFsScanWriter scanWriter = new GridFsScanWriter();
        scanWriter.setTemplate(gridFsTemplate);
        scanWriter.setScanStore(scanStore());
        return scanWriter;
    }

    /**
     * @return cleaner of loaded scan images
     */
    @Bean
    public InboxScanCleaner inboxScanCleaner() {
        InboxScanCleaner cleaner = new InboxScanCleaner();
        cleaner.setBaseDir(INBOX_DIR);
        cleaner.setScanStore(scanStore());
        return cleaner;
    }

    /**
     * @return reader of files from inbox folder
     */
    @Bean
    public ItemReader<Signature> inboxTranscriptReader() {
        InboxTranscriptReader transcriptsReader = new InboxTranscriptReader();
        transcriptsReader.setBaseDir(INBOX_DIR);
        transcriptsReader.setResourceLoader(resourceLoader);
        transcriptsReader.setScanStore(scanStore());
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
     * @return item writer for persons to mongo
     */
    @Bean
    public ItemWriter<Signature> signatureMongoWriter() {
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
        InboxTranscriptCleaner cleaner = new InboxTranscriptCleaner();
        cleaner.setBaseDir(INBOX_DIR);
        cleaner.setScanStore(scanStore());
        return cleaner;
    }

}
