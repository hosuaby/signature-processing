package io.hosuaby.signatures.generators;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import io.codearte.jfairy.producer.person.Address;
import io.hosuaby.signatures.domain.SignSvg;
import io.hosuaby.signatures.domain.Signature;
import io.hosuaby.signatures.generators.config.BatikConfig;

/**
 * Producer of signatures directly to the "inbox" collection in Mongo DB.
 * Simulates collection of signatures on electronic devices.
 */
@Component
public class SignatureInboxProducer {

    /** Production rate */
    private static final int RATE = 1000;

    /** Signature generator */
    @Autowired
    private RandomSignatureGenerator signatureGenerator;

    /** DOM implementation */
    @Autowired
    private DOMImplementation domImpl;

    /** Mongo template */
    @Autowired
    private MongoOperations mongoTemplate;

    /**
     * Produces random signature and add it directly to "inbox" collection.
     */
    @Scheduled(fixedRate = RATE)
    public void produce() {
        RandomSignature randomSignature = signatureGenerator.randomSignature();
        Signature signature = convert(randomSignature);
        mongoTemplate.save(signature, "inbox");
    }

    /**
     * Converts {@code RandomSignature} to {@code Signature}.
     * @param randomSignature    random signature
     * @return {@code Signature} object
     */
    private Signature convert(RandomSignature randomSignature) {
        Signature signature = new Signature();

        /* Birth date */
        Date tmpBirthDate = randomSignature.getPerson().dateOfBirth().toDate();
        LocalDate birthDate = tmpBirthDate
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        /* Address */
        Address adr = randomSignature.getPerson().getAddress();
        String address = adr.streetNumber() + ", " + adr.street() + ", "
                + adr.getPostalCode() + ", " + adr.getCity();

        /* Signature date */
        // TODO: fix signature date
//        Date tmpSignatureDate = randomSignature.getSignatureDate().toDate();
//        LocalDate signatureDate = tmpSignatureDate
//                .toInstant()
//                .atZone(ZoneId.systemDefault())
//                .toLocalDate();

        signature.setFirstName(randomSignature.getPerson().firstName());
        signature.setLastName(randomSignature.getPerson().lastName());
        signature.setBirthDate(birthDate);
        signature.setAddress(address);
        signature.setIdCardNumber(randomSignature.getPerson().nationalIdentityCardNumber());

        // TODO: set sign
        Document document = domImpl.createDocument(
                BatikConfig.SVG_NAMESPACE_URI, "svg", null);

        SVGGraphics2D graphics = new SVGGraphics2D(document);
        graphics.draw(randomSignature.getSign());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out);

        String xml = "";

        try {
            graphics.stream(writer);
            xml = new String(out.toByteArray());
        } catch (SVGGraphics2DIOException e) {
            e.printStackTrace();
        }

        SignSvg sign = new SignSvg();
        sign.setData(xml);

        signature.setSign(sign);

        return signature;
    }

}
