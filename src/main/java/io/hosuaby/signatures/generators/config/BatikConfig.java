package io.hosuaby.signatures.generators.config;

import java.awt.Color;
import java.util.Random;

import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.w3c.dom.DOMImplementation;

/**
 * Defines beans used for SVG generation by Batik.
 */
@Configuration
public class BatikConfig {

    /**
     * SVG namespace URI
     */
    public static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";

    /**
     * @return Default DOM implementation
     */
    @Bean
    public DOMImplementation domImpl() {
        return GenericDOMImplementation.getDOMImplementation();
    }

    /**
     * @return Converter from SVG to raster image.
     */
    @Bean
    public SVGConverter svgConverter() {
        return new SVGConverter() {{
            setDestinationType(DestinationType.PNG);
            setQuality((float) 0.99);
            setBackgroundColor(Color.WHITE);
            setWidth((float) 1000.0);
            setHeight((float) 2100.0);
        }};
    }

    /**
     * @return randomizer
     */
    @Bean
    public Random randomizer() {
        return new Random();
    }

}
