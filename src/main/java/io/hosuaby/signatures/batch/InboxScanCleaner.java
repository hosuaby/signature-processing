package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;

import io.hosuaby.signatures.config.BatchConfig;

/**
 * Cleaner of loaded scan images from inbox folder.
 */
public class InboxScanCleaner extends StepExecutionListenerSupport {

    /** Scan store */
    @Resource(name = "scanStore")
    private Map<String, BufferedImage> scanStore;

    /**
     * Deletes loaded image files.
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        for (Entry<String, BufferedImage> entry : scanStore.entrySet()) {
            if (entry.getValue() != null) {
                new File(BatchConfig.INBOX_DIR + entry.getKey() + ".png")
                        .delete();
            }
        }

        return stepExecution.getExitStatus();
    }

}
