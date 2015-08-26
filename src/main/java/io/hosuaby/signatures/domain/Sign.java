package io.hosuaby.signatures.domain;

/**
 * Sign.
 */
public abstract class Sign {

    /** PNG */
    public static final String FORMAT_PNG = "png";

    /** SVG */
    public static final String FORMAT_SVG = "svg";

    /**
     * @return format of the sign image.
     */
    public abstract String getFormat();

}
