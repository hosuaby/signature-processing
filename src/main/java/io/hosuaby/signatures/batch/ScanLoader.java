package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import io.hosuaby.signatures.domain.Scan;

/**
 * {@link ItemProcessor} implementation that loads {@link BufferedImage} of
 * scan from inbox directory found by it's list ID, stores it into
 * {@code scanStore} and returns created {@link Scan} instance.
 */
public class ScanLoader implements ItemProcessor<String, Scan>,
        InitializingBean {

    /** Error message when base directory is not provided */
    private static final String ERR_INBOX_DIR_NULL = "Inbox directory must be provided";

    /** Error message when scan store is not provided */
    private static final String ERR_SCAN_STORE_NULL = "Scan store must be provided";

    /** Path to the inbox directory */
    private String inboxDir;

    /** Scan store */
    private Map<String, BufferedImage> scanStore;

    @Override
    public Scan process(String id) throws Exception {
        BufferedImage scan = ImageIO.read(new File(inboxDir + id + ".png"));
        scanStore.put(id, scan);
        return new Scan(id, scan);
    }

    /**
     * Sets inbox directory for reader.
     *
     * @param inboxDir    path to inbox directory
     */
    public void setInboxDir(String inboxDir) {
        this.inboxDir = inboxDir;

        if (inboxDir.charAt(inboxDir.length() - 1) != '/') {
            this.inboxDir += '/';
        }
    }

    /**
     * Sets scan store.
     *
     * @param scanStore    scan store
     */
    public void setScanStore(Map<String, BufferedImage> scanStore) {
        this.scanStore = scanStore;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(inboxDir, ERR_INBOX_DIR_NULL);
        Assert.notNull(scanStore, ERR_SCAN_STORE_NULL);
    }

}
