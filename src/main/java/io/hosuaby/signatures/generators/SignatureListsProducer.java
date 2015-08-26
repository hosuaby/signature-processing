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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.hosuaby.signatures.generators.config.ProducersConfig;

/**
 * Producer of images of lists with signatures in regular time interval.
 * Simulates collection of signattures on the paper list.
 */
@Component
public class SignatureListsProducer {

    /** Signature generator */
    @Autowired
    private RandomSignatureGenerator signatureGenerator;

    /** Render of list of signatures as SVG */
    @Autowired
    private SignaturesListSvgRender signatureListSvgRender;

    /** SVG to raster converter */
    @Autowired
    private SVGConverter svgConverter;

    /**
     * Creates base directory if it not exists.
     */
    @PostConstruct
    public void createDir() {
        new File(ProducersConfig.BASE_DIR).mkdirs();
    }

    /**
     * Produces list of signatures with fixed rate.
     */
    @Scheduled(fixedRate = ProducersConfig.PRODUCTION_RATE)
    public void produce() {

        /* Generate random list ID */
        String listId = UUID.randomUUID().toString();

        /* Filename */
        String filename = ProducersConfig.BASE_DIR + listId.toString();

        /* Generate random signatures */
        List<RandomSignature> signatures = signatureGenerator
                .randomSignatures(30);

        /* Render list of signatures to SVG document */
        SVGGraphics2D rendered = signatureListSvgRender.render(signatures);

        try {

            /* Create the "lock" file for new list */
            File lock = new File(filename + ".lock");
            lock.createNewFile();

            /* Create the SVG file with signatures */
            File svg = new File(filename + ".svg");
            rendered.stream(new FileWriter(svg), false);

            /* Convert SVG -> PNG */
            svgConverter.setSources(new String[] { filename + ".svg" });
            svgConverter.setDst(new File(filename + ".png"));
            svgConverter.execute();

            /* Delete original SVG file */
            svg.delete();

            /* Write transcriptions of signatures to text file */
            FileWriter fileWriter = new FileWriter(new File(filename + ".txt"));
            for (RandomSignature signature : signatures) {
                fileWriter.write(signature.toString() + "\n");
            }
            fileWriter.close();

            /* Remove the "lock" file and allow to batch process this list */
            lock.delete();
        } catch (IOException | SVGConverterException e) {
            e.printStackTrace();
        }
    }

}
