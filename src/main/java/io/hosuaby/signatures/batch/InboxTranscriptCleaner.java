package io.hosuaby.signatures.batch;

import java.io.File;
import java.util.ArrayList;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Cleaner of loaded transcript files from inbox folder.
 */
//TODO: make read execution context instead of list IDs store
//TODO: do the same logic in writer with @AfterStep annotation
public class InboxTranscriptCleaner extends StepExecutionListenerSupport
        implements InitializingBean {

    /** Error message when base directory is not provided */
    private static final String ERR_INBOX_DIR_NULL = "Inbox directory must be provided";

    /** Error message when list ids store is not provided */
    private static final String ERR_LIST_IDS_STORE_NULL = "List IDs store must be provided";

    /** Path to the inbox directory */
    private String inboxDir;

    /** List IDs store */
    private ArrayList<String> listIdsStore;

    /**
     * Deletes loaded transcript text files.
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        for (String listId : listIdsStore) {
            new File(inboxDir + listId + ".txt").delete();
        }

        return stepExecution.getExitStatus();
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
     * Sets list IDs store.
     *
     * @param listIdsStore    list IDs store
     */
    public void setListIdsStore(ArrayList<String> listIdsStore) {
        this.listIdsStore = listIdsStore;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(inboxDir, ERR_INBOX_DIR_NULL);
        Assert.notNull(listIdsStore, ERR_LIST_IDS_STORE_NULL);
    }

}
