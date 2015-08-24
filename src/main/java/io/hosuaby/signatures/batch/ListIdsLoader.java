package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

import io.hosuaby.signatures.config.BatchConfig;

/**
 * Loads IDs of lists of signatures that must be processed by job and stores it
 * in job scoped store.
 */
public class ListIdsLoader extends JobExecutionListenerSupport {

    /** Scan store */
    @Resource(name = "scanStore")
    private Map<String, BufferedImage> scanStore;

    /**
     * Loads & stores IDs of processed lists into scan store.
     */
    @Override
    public void beforeJob(JobExecution exec) {
        File dir = new File(BatchConfig.INBOX_DIR);

        String[] filenames = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        });

        Arrays.asList(filenames)
            .stream()
            . <String> map(filename -> filename.split("\\.")[0])
            .filter(listId -> !new File(BatchConfig.INBOX_DIR + listId
                    + ".lock").exists())
            .forEach(listId -> scanStore.put(listId, null));
    }

    /**
     * Clears scan store.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        scanStore.clear();
    }

}
