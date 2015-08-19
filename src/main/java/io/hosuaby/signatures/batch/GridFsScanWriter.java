package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.Assert;

/**
 * Write for scan images to Mongo GridFS.
 */
public class GridFsScanWriter
        extends AbstractItemStreamItemWriter<String>
        implements InitializingBean {

    /** Store not provided message */
    private static final String ERR_STORE_NOT_PROVIDED = "Scan store required";

    /** Message of exception raised when GridFsTemplate was not set */
    private static final String ERR_GRIDFS_TEMPLATE_UNSET = "A GridFsTemplate implementation is required";

    /** Wrong item number message */
    private static final String ERR_WRONG_ITEM_NUMBER = "Number of items not match number of elements in ID store";

    /** Store for ids of processed lists */
    private Map<String, BufferedImage> scanStore;

    /** GridFS template */
    private GridFsTemplate template;

    public void setScanStore(Map<String, BufferedImage> scanStore) {
        this.scanStore = scanStore;
    }

    /**
     * Set the {@link GridFsTemplate} to be used to save items to be written.
     *
     * @param template the template implementation to be used.
     */
    public void setTemplate(GridFsTemplate template) {
        this.template = template;
    }

    @Override
    public void write(List<? extends String> listIds) throws Exception {
        for (String listId : listIds) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(scanStore.get(listId), "png", outputStream);
            InputStream inputStream =
                    new ByteArrayInputStream(outputStream.toByteArray());
            template.store(inputStream, listId);
        }
    }

    /**
     * Checks that GridFsTemplate was set.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(scanStore != null, ERR_STORE_NOT_PROVIDED);
        Assert.state(template != null, ERR_GRIDFS_TEMPLATE_UNSET);
    }

}
