package io.hosuaby.signatures.batch;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.springframework.batch.item.ItemProcessor;

import io.hosuaby.signatures.domain.SignPng;
import io.hosuaby.signatures.domain.Signature;

/**
 * Processor that adds sign image to the {@code Signature} object.
 */
public class SignatureSignAdder implements ItemProcessor<Signature, Signature> {

    /** Scan store */
    @Resource(name = "scanStore")
    private Map<String, BufferedImage> scanStore;

    @Override
    public Signature process(Signature signature) throws Exception {
        BufferedImage scan = scanStore.get(signature.getListId());
        int lineNumber = signature.getLineNumber();

        if (scan != null) {
            BufferedImage sign = scan.getSubimage(
                    920, (lineNumber - 1) * 70, 70, 70);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(sign, "png", baos);
            baos.flush();
            byte[] bytes = baos.toByteArray();
            baos.close();

            SignPng singPng = new SignPng();
            singPng.setData(bytes);

            signature.setSign(singPng);
        }

        return signature;
    }

}
