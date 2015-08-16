package io.hosuaby.signatures.generators;

import java.awt.Shape;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import io.codearte.jfairy.producer.person.Address;
import io.codearte.jfairy.producer.person.Person;

/**
 * Generated random signature.
 */
public class RandomSignature {

    /** Date formatter */
    private static final DateTimeFormatter formatter = DateTimeFormat
            .forPattern("yyyy-MM-dd");

    /** Fake person identity */
    private Person person;

    /** Date of signature */
    private DateTime signatureDate;

    /** Sign */
    private Shape sign;

    /**
     * @return String ready to be inserted into text file of signatures.
     */
    @Override
    public String toString() {
        Address address = person.getAddress();
        return person.firstName() + ";" + person.lastName() + ";"
                + formatter.print(person.dateOfBirth()) + ";"
                + address.streetNumber() + ", " + address.street() + ", "
                + address.getPostalCode() + ", " + address.getCity() + ";"
                + person.nationalIdentityCardNumber() + ";"
                + formatter.print(signatureDate);
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public DateTime getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(DateTime signatureDate) {
        this.signatureDate = signatureDate;
    }

    public Shape getSign() {
        return sign;
    }

    public void setSign(Shape sign) {
        this.sign = sign;
    }

}
