package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import io.hosuaby.signatures.domain.Signature;

/**
 * Reader of signatures from transcript files.
 */
public class InboxTranscriptReader
        extends AbstractItemStreamItemReader<Signature>
        implements InitializingBean, ResourceLoaderAware {

    /** Delegete not provided message */
    private static final String ERR_DELEGATE_NOT_PROVIDED = "Delegate ItemReader is not provided";

    /** Base dir not provided message */
    private static final String ERR_BASE_DIR_NOT_PROVIDED = "Base dir is not provided";

    /** Resource loader not provided message */
    private static final String ERR_RESOURCE_LOADER_NOT_PROVIDED = "Resource loader not provided";

    /** Store not provided message */
    private static final String ERR_STORE_NOT_PROVIDED = "Scan store required";

    /** Delegate item reader */
    private ResourceAwareItemReaderItemStream<? extends Signature> delegate;

    /** Base directory for images */
    private String baseDir;

    /** Spring resource loader */
    private ResourceLoader resourceLoader;

    /** Store for ids of processed lists */
    private Map<String, BufferedImage> scanStore;

    /** Iterator over list ids */
    private Iterator<String> iterator = null;

    /** Iteration over list ids started */
    private boolean started = false;

    /** Current list ID */
    private String currentListId = null;

    public InboxTranscriptReader() {
        this.setExecutionContextName(ClassUtils.getShortName(MultiResourceItemReader.class));
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        super.open(executionContext);
        this.iterator = scanStore.keySet().iterator();
        this.started = false;
    }

    @Override
    public Signature read() throws Exception, UnexpectedInputException,
            ParseException, NonTransientResourceException {
        if (!started) {
            if (iterator.hasNext()) {
                currentListId = iterator.next();
                Resource resource =  resourceLoader
                        .getResource("file:" + baseDir + currentListId
                                + ".txt");
                delegate.setResource(resource);
                delegate.open(new ExecutionContext());
            } else {
                return null;
            }
        }
        return readNextSignature();
    }

    private Signature readNextSignature() throws Exception {
        Signature signature = delegate.read();

        while (signature == null) {
            delegate.close();

            if (iterator.hasNext()) {
                currentListId = iterator.next();
                Resource resource =  resourceLoader
                        .getResource("file:" + baseDir + currentListId
                                + ".txt");
                delegate.setResource(resource);
                delegate.open(new ExecutionContext());
            } else {
                return null;
            }

            signature = delegate.read();
        }

        signature.setListId(currentListId);
        return signature;
    }

    public void setDelegate(ResourceAwareItemReaderItemStream<? extends Signature> delegate) {
        this.delegate = delegate;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;

        /* Add trailing slash if necessary */
        if (this.baseDir.charAt(this.baseDir.length() - 1) != '/') {
            this.baseDir += '/';
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setScanStore(Map<String, BufferedImage> scanStore) {
        this.scanStore = scanStore;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(delegate != null, ERR_DELEGATE_NOT_PROVIDED);
        Assert.state(baseDir != null && !baseDir.isEmpty(),
                ERR_BASE_DIR_NOT_PROVIDED);
        Assert.state(resourceLoader != null, ERR_RESOURCE_LOADER_NOT_PROVIDED);
        Assert.state(scanStore != null, ERR_STORE_NOT_PROVIDED);
    }

}
