package io.hosuaby.signatures.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.apps.rasterizer.SVGConverterException;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Producer of lists with signatures with regular time interval.
 */
@Component
public class SignatureListsProducer {

    /** Rate of production */
    private static final long RATE = 5000;

    /** Base directory for signatures lists */
    private static final String BASE_DIR = "/tmp/signatures/lists/";

    /** Signature generator */
    @Autowired
    private RandomSignatureGenerator signatureGenerator;

    /** Creator of SVG image of signatures list */
    @Autowired
    private SignaturesSvgListCreator signatureSvgListCreator;

    /** SVG to raster converter */
    @Autowired
    private SVGConverter svgConverter;

    /**
     * Creates base directory if it not exists.
     */
    @PostConstruct
    public void createDir() {
        new File(BASE_DIR).mkdirs();
    }

    /**
     * Produces list of signatures with fixed rate.
     */
    @Scheduled(fixedRate = RATE)
    public void produce() {

        /* UUID of the list */
        UUID uuid = UUID.randomUUID();

        String filename = BASE_DIR + uuid.toString();   // filename

        List<RandomSignature> signatures = signatureGenerator
                .randomSignatures(30);

        /* Create SVG image of the list of signatures */
        SVGGraphics2D graphics = signatureSvgListCreator.draw(signatures);

        try {
            graphics.stream(filename + ".svg", false);

            /* Convert SVG -> PNG */
            svgConverter.setSources(new String[] { filename + ".svg" });
            svgConverter.setDst(new File(filename + ".png"));
            svgConverter.execute();

            /* Delete original SVG file */
            new File(filename + ".svg").delete();
        } catch (SVGGraphics2DIOException | SVGConverterException e) {
            e.printStackTrace();
        }

        /* Write transcriptions of signatures to text file */
        try {
            FileWriter fileWriter = new FileWriter(new File(filename + ".txt"));
            for (RandomSignature signature : signatures) {
                fileWriter.write(signature.toString() + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
