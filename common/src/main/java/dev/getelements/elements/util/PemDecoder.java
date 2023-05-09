package dev.getelements.elements.util;

import dev.getelements.elements.annotation.PemFile;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.InvalidPemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.function.Function;

public class PemDecoder<KeySpecT extends KeySpec> {

    private static final Logger logger = LoggerFactory.getLogger(PemDecoder.class);

    private static final String HEADER = "-----BEGIN";

    private static final String FOOTER = "-----END";

    private final KeySpecT spec;

    public PemDecoder(final String pemString,
                      final Function<byte[], KeySpecT> keySpecFunction) throws InvalidPemException {
        this(new StringReader(pemString), keySpecFunction);
    }

    public PemDecoder(final Reader reader,
                      final Function<byte[], KeySpecT> keySpecFunction) throws InvalidPemException {
        this(new BufferedReader(reader), keySpecFunction);
    }

    public PemDecoder(final BufferedReader reader,
                      final Function<byte[], KeySpecT> keySpecFunction) throws InvalidPemException {

        final StringBuilder encoded = new StringBuilder();

        try {

            final String header = reader.readLine().trim();
            if (!header.startsWith(HEADER)) throw new InternalException("Invalid PEM format. Missing header");

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(FOOTER)) break;
                else encoded.append(line.trim());
            }

            if (line == null) throw new InvalidPemException("Invalid PEM format. Missing footer.");

        } catch (IOException ex) {
            throw new InvalidPemException(ex);
        }

        final byte[] bytes = Base64.getDecoder().decode(encoded.toString());
        this.spec = keySpecFunction.apply(bytes);

    }

    public KeySpecT getSpec() {
        return spec;
    }

    public static class Validator implements ConstraintValidator<PemFile, String> {

        @Override
        public void initialize(final PemFile constraintAnnotation) {}

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext context) {
            try {
                final PemDecoder<KeySpec> pemDecoder = new PemDecoder(value, dummy -> new KeySpec(){});
                logger.trace("Successfully decoded pem {}", pemDecoder);
                return true;
            } catch (InvalidPemException e) {
                context.buildConstraintViolationWithTemplate("Invalid PEM File.");
                return false;
            }
        }

    }

}
