package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;

import io.hosuaby.signatures.config.BatchConfig;

/**
 * Loads scan images into store. Operation {@code read} returns id of every
 * loaded scan.
 */
// TODO: optimize this class
public class InboxScanReader extends AbstractItemStreamItemReader<String> {

    /** Scan store */
    @Resource(name = "scanStore")
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
                    new File(BatchConfig.INBOX_DIR + listId + ".png"));
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

}
