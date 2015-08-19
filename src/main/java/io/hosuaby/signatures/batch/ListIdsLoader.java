package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Map;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Loads ids of lists of signatures that must be processed by job and stores it
 * in job scoped store.
 */
public class ListIdsLoader
        extends JobExecutionListenerSupport
        implements InitializingBean {

    /** Base dir not provided message */
    private static final String ERR_BASE_DIR_NOT_PROVIDED = "Base dir is not provided!";

    /** Store not provided message */
    private static final String ERR_STORE_NOT_PROVIDED = "Scan store required";

    /** Base directory for images */
    private String baseDir;

    /** Store for ids of processed lists */
    private Map<String, BufferedImage> scanStore;

    @Override
    public void beforeJob(JobExecution exec) {
        File dir = new File(baseDir);

        String[] filenames = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        });

        Arrays.asList(filenames)
            .stream()
            . <String> map(filename -> filename.split("\\.")[0])
            .filter(listId -> !new File(baseDir + listId + ".lock").exists())
            .forEach(listId -> scanStore.put(listId, null));
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
