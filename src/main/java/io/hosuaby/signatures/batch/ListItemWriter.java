package io.hosuaby.signatures.batch;

import java.util.List;

import org.iban4j.support.Assert;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;

/**
 * Writer that adds items into provided {@link List} instance.
 *
 * @param <T>    type of items
 */
public class ListItemWriter<T> implements ItemWriter<T>, InitializingBean {

    /** Error message when list is not provided */
    private static final String ERR_LIST_NULL = "List must be provided";

    /** List where items must be added */
    private List<T> list;

    @Override
    public void write(List<? extends T> items) throws Exception {
        list.addAll(items);
    }

    /**
     * Sets list where items must be added.
     *
     * @param list    {@link List} instance
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(list, ERR_LIST_NULL);
    }

}
