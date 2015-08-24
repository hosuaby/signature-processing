package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

import io.hosuaby.signatures.config.BatchConfig;
import io.hosuaby.signatures.domain.Signature;

/**
 * Reader of signatures from transcript files.
 */
public class InboxTranscriptReader
        extends AbstractItemStreamItemReader<Signature> {

    /** Spring resource loader */
    @Autowired
    private ResourceLoader resourceLoader;

    /** Scan store */
    @Resource(name = "scanStore")
    private Map<String, BufferedImage> scanStore;

    /** Delegate item reader */
    private ResourceAwareItemReaderItemStream<? extends Signature> delegate;

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
            delegate.close();
            if (iterator.hasNext()) {
                currentListId = iterator.next();
                org.springframework.core.io.Resource resource =  resourceLoader
                        .getResource("file:" + BatchConfig.INBOX_DIR
                                + currentListId + ".txt");
                delegate.setResource(resource);
                delegate.open(new ExecutionContext());
                started = true;
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
                org.springframework.core.io.Resource resource =  resourceLoader
                        .getResource("file:" + BatchConfig.INBOX_DIR
                                + currentListId + ".txt");
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

}
