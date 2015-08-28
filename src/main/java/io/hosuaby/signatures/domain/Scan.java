package io.hosuaby.signatures.domain;

import java.awt.image.BufferedImage;

/**
 * Scan of signature list.
 */
public class Scan {

    /** List ID */
    private String listId;

    /** Image */
    private BufferedImage image;

    public Scan(String listId, BufferedImage image) {
        super();
        this.listId = listId;
        this.image = image;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
