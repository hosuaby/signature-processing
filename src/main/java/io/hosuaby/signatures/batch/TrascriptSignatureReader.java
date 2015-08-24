package io.hosuaby.signatures.batch;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.core.io.Resource;
import org.springframework.format.Formatter;
import org.springframework.validation.DataBinder;

import io.hosuaby.signatures.domain.Signature;

/**
 * Custom reader of signature from flat text file.
 */
public class TrascriptSignatureReader extends FlatFileItemReader<Signature> {

    /** List ID - name of resource file */
    private String listId;

    /**
     * Sets custom line mapper.
     */
    public TrascriptSignatureReader() {
        super();
        setLineMapper(lineMapper());
    }

    /**
     * Sets list id from the name of resource.
     */
    @Override
    protected Signature doRead() throws Exception {
        Signature signature = super.doRead();
        if (signature != null) {
            signature.setListId(listId);
        }
        return signature;
    }

    /**
     * Set list ID from the name of resource file
     */
    @Override
    public void setResource(Resource resource) {
        super.setResource(resource);
        listId = resource.getFilename().split("\\.")[0];
    }

    /**
     * @return line tokenizer
     */
    private LineTokenizer tokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(";");
        tokenizer.setNames(new String[] { "firstName", "lastName", "birthDate",
                "address", "idCardNumber", "signatureDate" });
        return tokenizer;
    }

    /**
     * @return line mapper
     */
    private LineMapper lineMapper() {
        DefaultLineMapper<Signature> lineMapper = new SignatureLineMapper();
        lineMapper.setLineTokenizer(tokenizer());
        lineMapper.setFieldSetMapper(new SignatureFieldSetMapper());
        return lineMapper;
    }

    /**
     * Signature line mapper.
     */
    private class SignatureLineMapper extends DefaultLineMapper<Signature> {

        /**
         * Sets lineNumber of read signature.
         */
        @Override
        public Signature mapLine(String line, int lineNumber) throws Exception {
            Signature signature = super.mapLine(line, lineNumber);
            signature.setLineNumber(lineNumber);
            return signature;
        }

    }

    /**
     * Filed set mapper for signature.
     */
    private class SignatureFieldSetMapper
            extends BeanWrapperFieldSetMapper<Signature> {

        /**
         * Sets target type.
         */
        public SignatureFieldSetMapper() {
            super();
            setTargetType(Signature.class);
        }

        /**
         * Redefined {@code initBinder}.
         */
        @Override
        protected void initBinder(DataBinder binder) {
            binder.addCustomFormatter(new Formatter<LocalDate>() {

                /** Not used */
                @Override
                public String print(LocalDate date, Locale locale) {
                    return null;
                }

                /** Parses date with provided format */
                @Override
                public LocalDate parse(String text, Locale locale)
                        throws ParseException {
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    return LocalDate.parse(text, formatter);
                }

            });
        }

    }

}
