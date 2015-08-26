package io.hosuaby.signatures.domain;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * PNG image of sign.
 */
public class SignPng extends Sign {

    /** Bytes of image */
    private byte[] data;

    /**
     * @return {@code BufferedImage} with signature.
     */
    public BufferedImage asImage() {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        try {
            return ImageIO.read(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getFormat() {
        return FORMAT_PNG;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
