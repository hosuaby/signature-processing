package io.hosuaby.signatures.generators;

import java.awt.Font;
import java.util.List;
import java.util.Random;

import org.apache.batik.svggen.SVGGraphics2D;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import io.codearte.jfairy.producer.person.Address;
import io.hosuaby.signatures.generators.config.BatikConfig;

/**
 * Creates SVG document from the list of random signatures.
 */
@Component
public class SignaturesSvgListCreator {

    /** Date format patterns */
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd", "dd-MM-yyyy", "dd-MM-yy", "dd.MM.yyyy",
            "dd MMMM yyyy"
    };

    /** DOM implementation */
    @Autowired
    private DOMImplementation domImpl;

    /** Randomizer */
    @Autowired
    private Random randomizer;

    public SVGGraphics2D draw(List<RandomSignature> signatures) {

      /* Create SVG path */
      Document document = domImpl.createDocument(
              BatikConfig.SVG_NAMESPACE_URI, "svg", null);

      SVGGraphics2D graphics = new SVGGraphics2D(document);

      Font font = graphics.getFont();

      for (int i = 0; i < signatures.size(); i++) {
          RandomSignature signature = signatures.get(i);
          int baseY = i * 70;
          String dateFormat = randomDateFormat();
          final DateTimeFormatter formatter = DateTimeFormat
                  .forPattern(dateFormat);

          /* Chose the font */
          if (randomizer.nextBoolean()) {

              /* Keep the normal font */

          } else if (randomizer.nextBoolean()) {

              /* Make font italic */
              graphics.setFont(font.deriveFont(Font.ITALIC));
          } else {

              /* Make font bold */
              graphics.setFont(font.deriveFont(Font.BOLD));
          }

          /* Draw person's name */
          graphics.drawString(getFullName(signature), 10, baseY + 35);

          /* Draw person's birth date */
          graphics.drawString(
                  formatter.print(signature.getPerson().dateOfBirth()),
                  210,
                  baseY + 35);

          /* Draw person's address */
          String[] brokenAddress = getAddress(signature);
          graphics.drawString(brokenAddress[0], 345, baseY + 35);
          graphics.drawString(brokenAddress[1], 345, baseY + 50);

          /* Draw person's ID number */
          graphics.drawString(
                  signature.getPerson().nationalIdentityCardNumber(),
                  690,
                  baseY + 35);

          /* Draw signature date */
          graphics.drawString(
                  formatter.print(signature.getSignatureDate()),
                  800,
                  baseY + 35);

          /* Draw sign */
          SVGGraphics2D gg = (SVGGraphics2D) graphics.create();
          gg.translate(920, baseY);
          gg.draw(signature.getSign());

          /* Separator line */
          graphics.drawLine(0, baseY + 70, 1000, baseY + 70);

          /* Reset ordinary font */
          graphics.setFont(font);
      }

      return graphics;
    }

    /**
     * Returns the full name from signature.
     * @param signature    signature object
     * @return the full name of person from signature
     */
    private String getFullName(RandomSignature signature) {
        StringBuilder builder = new StringBuilder();

        String firstName = ucfirst(signature.getPerson().firstName());
        String lastName  = signature.getPerson().lastName();

        if (randomizer.nextBoolean()) {

            /* Normal order and case */
            return firstName + " " + lastName;

        } else if (randomizer.nextBoolean()) {

            /* We keep last name capitalized but change the order */
            return lastName + " " + firstName;
        } else {

            /* Only first latter of the last name must be capitalized */
            lastName = ucfirst(lastName);

            /* Order first last names must not be changed */
            return firstName + " " + lastName;
        }
    }

    /**
     * Breaks address on two lines. Returns array of Strings with two elements.
     * @param address    initial address string
     * @return array of two strings with text of the address
     */
    private String[] getAddress(RandomSignature signature) {
        Address address = signature.getPerson().getAddress();


//        String[] parts = signature.getAddress().split(", ");
//        int halfIndex = parts.length / 2;
        String[] broken = new String[2];

        broken[0] = address.streetNumber() + ", " + address.street() + ",";
        broken[1] = address.getPostalCode() + ", " + address.getCity();

//        broken[0] = String.join(
//                ", ", Arrays.copyOfRange(parts, 0, halfIndex));
//        broken[1] = String.join(
//                ", ", Arrays.copyOfRange(parts, halfIndex, parts.length));

        return broken;
    }

    /**
     * Capitalizes the first letter of the string and uncapitalizes all other
     * characters.
     * @param str    text string
     * @return transformed text string
     */
    private String ucfirst(String str) {
        return str.substring(0, 1).toUpperCase()
                + str.substring(1).toLowerCase();
    }

    /**
     * @return randomly selected date format
     */
    private String randomDateFormat() {
        int i = randomizer.nextInt(DATE_FORMATS.length);
        return DATE_FORMATS[i];
    }

}
