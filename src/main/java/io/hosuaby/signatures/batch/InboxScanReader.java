package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Loads scan images into store. Operation {@code read} returns id of every
 * loaded scan.
 */
public class InboxScanReader
        extends AbstractItemStreamItemReader<String>
        implements InitializingBean {

    /** Base dir not provided message */
    private static final String ERR_BASE_DIR_NOT_PROVIDED = "Base dir is not provided!";

    /** Store not provided message */
    private static final String ERR_STORE_NOT_PROVIDED = "Scan store required";

    /** Base directory for images */
    private String baseDir;

    /** Store for ids of processed lists */
    private Map<String, BufferedImage> scanStore;

    /** Iterator over the the store */
    private Iterator<String> iterator;

    /** Counter of loaded resources */
    private AtomicInteger counter = new AtomicInteger(0);

    /**
     * Constructor. Initialize the name for the key in the execution context.
     */
    public InboxScanReader() {
        this.setExecutionContextName(getClass().getName());
    }

    /**
     * Loads the scan into store. .Increments a counter and returns the next
     * loaded list ID.
     */
    @Override
    public synchronized String read() throws Exception {
        counter.incrementAndGet();

        if (iterator.hasNext()) {
            String listId = iterator.next();
            BufferedImage image = ImageIO.read(
                    new File(baseDir + listId + ".png"));
            scanStore.put(listId, image);
            return listId;
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void open(ExecutionContext executionContext)
            throws ItemStreamException {
        super.open(executionContext);
        counter.set(executionContext.getInt(getExecutionContextKey("COUNT"), 0));
        iterator = scanStore.keySet().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ExecutionContext executionContext)
            throws ItemStreamException {
        super.update(executionContext);
        executionContext.putInt(getExecutionContextKey("COUNT"), counter.get());
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
