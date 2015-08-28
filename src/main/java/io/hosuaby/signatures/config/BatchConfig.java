package io.hosuaby.signatures.config;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.JobSynchronizationManager;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import io.hosuaby.signatures.batch.GridFsScanWriter;
import io.hosuaby.signatures.batch.InboxScanCleaner;
import io.hosuaby.signatures.batch.InboxTranscriptCleaner;
import io.hosuaby.signatures.batch.ListIdsItemReader;
import io.hosuaby.signatures.batch.ListItemWriter;
import io.hosuaby.signatures.batch.ScanLoader;
import io.hosuaby.signatures.batch.SignatureSignAdder;
import io.hosuaby.signatures.batch.TrascriptSignatureReader;
import io.hosuaby.signatures.domain.Scan;
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

    /** GridFS template */
    @Autowired
    private GridFsTemplate gridFsTemplate;

    /**
     * @return store for IDs of processed lists
     */
    @Bean
    @JobScope
    public ArrayList<String> listIdsStore() {
        return new ArrayList<String>();
    }

    /**
     * @return store for scan images
     */
    @Bean
    @JobScope
    public Map<String, BufferedImage> scanStore() {
        return Collections.synchronizedMap(
                new HashMap<String, BufferedImage>());
    }

    /**
     * @return task executor
     *
     * @see https://jira.spring.io/browse/BATCH-2269
     */
    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void doExecute(Runnable task) {

                /* Gets the jobExecution of the configuration thread */
                JobExecution jobExecution = JobSynchronizationManager
                        .getContext()
                        .getJobExecution();

                super.doExecute(new Runnable() {
                    @Override
                    public void run() {
                        JobSynchronizationManager.register(jobExecution);

                        try {
                            task.run();
                        } finally {
                            JobSynchronizationManager.release();
                        }
                    }
                });
            }
        };
    }

    /**
     * @return partition handler
     */
    // TODO: code it later
//    @Bean
//    public PartitionHandler loadScansPartitionHandler() {
//        TaskExecutorPartitionHandler partitionHandler =
//                new TaskExecutorPartitionHandler();
//        partitionHandler.setTaskExecutor(taskExecutor());
//        partitionHandler.setStep(loadScans());
//        partitionHandler.setGridSize(10);
//        return partitionHandler;
//    }

    /**
     * @return main job
     */
    @Bean
    public Job job() {
        return jobs.get("mainJob")
                .incrementer(new RunIdIncrementer())
                .flow(loadProcessedListIds())
                .next(new FlowBuilder<SimpleFlow>("splitFlow")
                        .start(loadScans())
                        .split(taskExecutor())
                        .add(new FlowBuilder<SimpleFlow>("loadTranscriptsFlow")
                                .start(loadTranscripts())
                                .build())
                        .build())
                .next(addSign())
                .end()
                .build();
    }

    /**
     * @return step loading IDs of signature list that must be processed to
     *         listIdsStore.
     */
    @Bean
    public Step loadProcessedListIds() {
        // TODO: remove chunk
        return steps.get("loadProcessedListIds")
                .<String, String> chunk(10)
                .reader(listIdsItemReader())
                .writer(listIdsStoreItemWriter())
                .build();
    }

    /**
     * @return reader of IDs of signature lists available for processing in
     *         inbox directory
     */
    @Bean
    public ListIdsItemReader listIdsItemReader() {
        ListIdsItemReader reader = new ListIdsItemReader();
        reader.setInboxDir(INBOX_DIR);
        return reader;
    }

    /**
     * @return writer that writes IDs of signature lists into the store
     */
    @Bean
    public ListItemWriter<String> listIdsStoreItemWriter() {
        ListItemWriter<String> writer = new ListItemWriter<>();
        writer.setList(listIdsStore());
        return writer;
    }

    /**
     * @return step loading image scans and saving them into GridFS
     */
    @Bean
    public Step loadScans() {
        return steps.get("loadScans")
                . <String, Scan> chunk(10)
                .reader(listIdsStoreItemReader())
                .processor(scanLoader())
                .writer(gridFsScanWriter())
                .listener(inboxScanCleaner())
                .taskExecutor(taskExecutor())
                .throttleLimit(Runtime.getRuntime().availableProcessors())
                .build();
    }

    /**
     * @return list IDs item reader from listIdsStore
     */
    @Bean
    @StepScope
    public IteratorItemReader<String> listIdsStoreItemReader() {
        return new IteratorItemReader<String>(listIdsStore());
    }

    /**
     * @return loader of scans. Implements {@link ItemProcessor}
     */
    @Bean
    public ScanLoader scanLoader() {
        ScanLoader scanLoader = new ScanLoader();
        scanLoader.setInboxDir(INBOX_DIR);
        scanLoader.setScanStore(scanStore());
        return scanLoader;
    }

    /**
     * @return writer for images in Grid FS
     */
    @Bean
    public GridFsScanWriter gridFsScanWriter() {
        GridFsScanWriter writer = new GridFsScanWriter();
        writer.setTemplate(gridFsTemplate);
        return writer;
    }

    /**
     * @return cleaner of loaded scan images
     */
    @Bean
    public InboxScanCleaner inboxScanCleaner() {
        InboxScanCleaner cleaner = new InboxScanCleaner();
        cleaner.setInboxDir(INBOX_DIR);
        cleaner.setListIdsStore(listIdsStore());
        return cleaner;
    }

    /**
     * @return step loading transcripts of signature lists
     */
    @Bean
    public Step loadTranscripts() {
        return steps.get("loadTranscripts")
                . <Signature, Signature> chunk(10)
                .reader(transcriptSignatureItemReader())
                .writer(signaturePreloadMongoWriter())
                .listener(inboxTrascriptCleaner())
                .taskExecutor(taskExecutor())
                .throttleLimit(Runtime.getRuntime().availableProcessors())
                .build();
    }

    /**
     * @return reader of signatures from transcript files in inbox directory
     */
    @Bean
    @StepScope
    public MultiResourceItemReader<Signature> transcriptSignatureItemReader() {

        /* Get transcript resources */
        Resource[] resources = listIdsStore()
            .stream()
            . <Resource> map(listId ->
                    new FileSystemResource(INBOX_DIR + listId + ".txt"))
            .toArray(size -> new Resource[size]);

        /* Create MultiResourceItemReader */
        MultiResourceItemReader<Signature> multiResourceReader =
                new MultiResourceItemReader<>();
        multiResourceReader.setResources(resources);
        multiResourceReader.setDelegate(trascriptSignatureReader());

        return multiResourceReader;
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
     * @return cleaner of loaded scan images
     */
    @Bean
    public InboxTranscriptCleaner inboxTrascriptCleaner() {
        InboxTranscriptCleaner cleaner = new InboxTranscriptCleaner();
        cleaner.setInboxDir(INBOX_DIR);
        cleaner.setListIdsStore(listIdsStore());
        return cleaner;
    }

    /**
     * @return step adding sign image to signature
     */
    @Bean
    public Step addSign() {
        return steps.get("addSign")
                . <Signature, Signature> chunk(10)
                .reader(preloadSignatureMongoReader())
                .processor(signatureSignAdder())
                .writer(inboxSignatureMongoWriter())
                .build();
    }

    /**
     * @return reader of {@code Signature}s from collection "preload" in mongo
     */
    @Bean
    public MongoItemReader<Signature> preloadSignatureMongoReader() {
        MongoItemReader<Signature> reader = new MongoItemReader<>();
        reader.setTemplate(mongoTemplate);
        reader.setCollection("preload");
        reader.setTargetType(Signature.class);
        reader.setQuery("{}");
        reader.setSort(new HashMap<String, Direction>() {
            private static final long serialVersionUID = 1L;
            {
                put("listId", Direction.ASC);
                put("lineNumber", Direction.ASC);
            }
        });
        return reader;
    }

    /**
     * @return adder of sign to signature
     */
    @Bean
    public SignatureSignAdder signatureSignAdder() {
        SignatureSignAdder signAdder = new SignatureSignAdder();
        signAdder.setScanStore(scanStore());
        return signAdder;
    }

    /**
     * @return writer of {@link Signature}s to collection "inbox" of mongo
     */
    @Bean
    public ItemWriter<Signature> inboxSignatureMongoWriter() {
        MongoItemWriter<Signature> itemWriter = new MongoItemWriter<>();
        itemWriter.setTemplate(mongoTemplate);
        itemWriter.setCollection("inbox");
        return itemWriter;
    }

}
