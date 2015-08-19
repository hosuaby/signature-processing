package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Cleaner of loaded scan images from inbox folder.
 */
public class InboxScanCleaner
        extends StepExecutionListenerSupport
        implements InitializingBean {

    /** Base dir not provided message */
    private static final String ERR_BASE_DIR_NOT_PROVIDED = "Base dir is not provided!";

    /** Store not provided message */
    private static final String ERR_STORE_NOT_PROVIDED = "Scan store required";

    /** Base directory for images */
    private String baseDir;

    /** Store for ids of processed lists */
    private Map<String, BufferedImage> scanStore;

    /**
     * Deletes loaded image files.
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        for (Entry<String, BufferedImage> entry : scanStore.entrySet()) {
            if (entry.getValue() != null) {
                new File(baseDir + entry.getKey() + ".png").delete();
            }
        }

        return stepExecution.getExitStatus();
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;

        /* Add trailing slash if necessary */
        if (this.baseDir.charAt(this.baseDir.length() - 1) != '/') {
            this.baseDir += '/';
        }
    }

    public void setScanStore(Map<String, BufferedImage> scanStore) {
        this.scanStore = scanStore;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(baseDir != null && !baseDir.isEmpty(),
                ERR_BASE_DIR_NOT_PROVIDED);
        Assert.state(scanStore != null, ERR_STORE_NOT_PROVIDED);
    }

}
