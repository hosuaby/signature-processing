package io.hosuaby.signatures.generators;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMImplementation;

/**
 * Generator for random sign in SVG. Draws signature with one random SVG path
 * in the square 70x70 px.
 */
@Component
public class RandomSignSvgGenerator {

    /** Width of sign */
    private static final int WIDTH  = 70;

    /** Height of sign */
    private static final int HEIGHT = 70;

    /** DOM implementation */
    @Autowired
    private DOMImplementation domImpl;

    /** Randomizer */
    @Autowired
    private Random randomizer;

    /**
     * @return randomly created sign
     */
    public Shape randomSign() {
        GeneralPath path = new GeneralPath();

        /* Random number of points from 4 to 7 */
        int nbPoints = randomizer.nextInt(3) + 4;

        path.moveTo(rX(), rY());

        for (int i = 1; i < nbPoints; i++) {
            path.curveTo(rX(), rY(), rX(), rY(), rX(), rY());
        }

        return path;
    }

    /**
     * @return random x coordinate within drawing area
     */
    private int rX() {
        return randomizer.nextInt(WIDTH);
    }

    /**
     * @return random x coordinate within drawing area
     */
    private int rY() {
        return randomizer.nextInt(HEIGHT);
    }

}
