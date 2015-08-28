package io.hosuaby.signatures.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Map.Entry;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import io.hosuaby.signatures.config.BatchConfig;

/**
 * Reader of signature list IDs from inbox directory.
 */
public class ListIdsItemReader implements ItemReader<String>, ItemStream,
        InitializingBean {

    /** Key for execution context to put total number of list ids */
    private static final String IDS_NUMBER = "listIdsItemReader.number";

    /** Key for execution context to put list ids */
    private static final String LIST_ID = "listIdsItemReader.listId_";

    /** Number of currently processed id */
    private static final String CURRENT_ID_COUNT = "listIdsItemReader.current";

    /** Error message when base directory is not provided */
    private static final String ERR_INBOX_DIR_NULL = "Inbox directory must be provided";

    /** Path to the inbox directory */
    private String inboxDir;

    /** Total number of list ids */
    private int number;

    /** Currently processed id */
    private int count;

    /** List ids */
    private String[] ids;

    @Override
    public String read() throws Exception, UnexpectedInputException,
            ParseException, NonTransientResourceException {
        if (count < number) {
            return ids[count++];
        }

        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws
            ItemStreamException {
        if (executionContext == null) {
            throw new IllegalArgumentException();
        }

        if (executionContext.containsKey(IDS_NUMBER)) {
            number = executionContext.getInt(IDS_NUMBER);
            count = executionContext.getInt(CURRENT_ID_COUNT);
            ids = new String[number];

            for (int i = 0; i < number; i++) {
                ids[i] = executionContext.getString(LIST_ID + i);
            }
        } else {
            ids = loadListIds();
            number = ids.length;
            count = 0;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws
            ItemStreamException {

        /* Clear context */
        for (Entry<String, Object> entry : executionContext.entrySet()) {
            String key = entry.getKey();
            executionContext.remove(key);
        }

        executionContext.putInt(IDS_NUMBER, number);
        executionContext.putInt(CURRENT_ID_COUNT, count);

        for (int i = 0; i < number; i++) {
            executionContext.putString(LIST_ID + i, ids[i]);
        }
    }

    @Override
    public void close() throws ItemStreamException {
    }

    /**
     * Loads list ids from inbox directory.
     *
     * @return array of ids of signature lists ready for processing
     */
    private String[] loadListIds() {
        File dir = new File(BatchConfig.INBOX_DIR);

        String[] filenames = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        });

        return Arrays.asList(filenames)
            .stream()
            . <String> map(filename -> filename.split("\\.")[0])
            .filter(listId -> !new File(BatchConfig.INBOX_DIR + listId
                    + ".lock").exists())
            .toArray(size -> new String[size]);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(inboxDir, ERR_INBOX_DIR_NULL);
    }

}
