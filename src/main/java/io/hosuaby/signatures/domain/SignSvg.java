package io.hosuaby.signatures.domain;

import java.awt.image.BufferedImage;

/**
 * SVG image of sign.
 */
public class SignSvg extends Sign {

    /** XML of images */
    private String data;

    /**
     * @return {@code BufferedImage} with signature.
     */
    public BufferedImage asImage() {
        return null;
    }

    @Override
    public String getFormat() {
        return FORMAT_SVG;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
