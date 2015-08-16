package io.hosuaby.signatures;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // TODO: try to run batch from console
    @Override
    public void run(String... args) throws Exception {
//        SVGGraphics2D graphics = signatureSvgListGenerator.draw(signatureGenerator.randomSignatures(30));
//        graphics.stream("/tmp/sign.svg", false);
//
//        svgConverter.setSources(new String[] { "/tmp/sign.svg" });
//        svgConverter.setDst(new File("/tmp/sign.png"));
//
//        svgConverter.execute();

    }

}
