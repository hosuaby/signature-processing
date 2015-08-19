package io.hosuaby.signatures.domain;

import java.awt.image.BufferedImage;

/**
 * Scan of the list.
 */
public class ListScan {

    /** List id */
    private String listId;

    /** Scanned image */
    private BufferedImage image;

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
