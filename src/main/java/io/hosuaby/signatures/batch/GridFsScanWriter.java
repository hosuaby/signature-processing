package io.hosuaby.signatures.batch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.Assert;

import io.hosuaby.signatures.domain.Scan;

/**
 * Write for scan images to Mongo GridFS.
 */
public class GridFsScanWriter implements ItemWriter<Scan>, InitializingBean {

    /** Error message when GridFS template is not provided */
    private static final String ERR_GRIDFS_TEMPLATE_NULL = "GridFS template must be provided";

    /** GridFS template */
    private GridFsTemplate template;

    @Override
    public void write(List<? extends Scan> scans) throws Exception {
        for (Scan scan : scans) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(scan.getImage(), "png", outputStream);
            InputStream inputStream =
                    new ByteArrayInputStream(outputStream.toByteArray());
            template.store(inputStream, scan.getListId());
        }
    }

    /**
     * Sets GridFS template.
     *
     * @param template    GridFS template
     */
    public void setTemplate(GridFsTemplate template) {
        this.template = template;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(template, ERR_GRIDFS_TEMPLATE_NULL);
    }

}
