package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * Write for scan images to Mongo GridFS.
 */
public class GridFsScanWriter extends AbstractItemStreamItemWriter<String> {

    /** Scan store */
    @Resource(name = "scanStore")
    private Map<String, BufferedImage> scanStore;

    /** GridFS template */
    @Autowired
    private GridFsTemplate gridFs;

    @Override
    public void write(List<? extends String> listIds) throws Exception {
        for (String listId : listIds) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(scanStore.get(listId), "png", outputStream);
            InputStream inputStream =
                    new ByteArrayInputStream(outputStream.toByteArray());
            gridFs.store(inputStream, listId);
        }
    }

}
