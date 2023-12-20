package dev.getelements.elements.util;

import dev.getelements.elements.annotation.PemFile;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.InvalidPemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.*;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PemDecoder<KeySpecT> {

    private static final Logger logger = LoggerFactory.getLogger(PemDecoder.class);

    private static final String SUFFIX = "-----";

    private static final String HEADER = "-----BEGIN ";

    private static final String FOOTER = "-----END ";

    private final String label;

    private final KeySpecT spec;

    public PemDecoder(final String pemString,
                      final Function<byte[], KeySpecT> keySpecFunction) throws InvalidPemException {
        this(new StringReader(pemString), keySpecFunction);
    }

    public PemDecoder(final InputStream inputStream,
                      final Function<byte[], KeySpecT> keySpecFunction) throws InvalidPemException {
        this(new InputStreamReader(inputStream, UTF_8), keySpecFunction);
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

            if (!header.startsWith(HEADER) && header.endsWith(SUFFIX))
                throw new InternalException("Invalid PEM format. Missing or malformed header.");

            label = header.substring(HEADER.length(), header.length() - SUFFIX.length());

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(FOOTER) && line.endsWith(SUFFIX)) break;
                else encoded.append(line.trim());
            }

            if (line == null)
                throw new InvalidPemException("Invalid PEM format. Missing footer line.");

            final String endLabel = line.substring(FOOTER.length(), line.length() - SUFFIX.length());

            if (!label.equals(endLabel))
                throw new InvalidPemException("Invalid PEM format. Header and footer labels do not match.");

        } catch (IOException ex) {
            throw new InvalidPemException(ex);
        }

        final byte[] bytes = Base64.getDecoder().decode(encoded.toString());
        this.spec = keySpecFunction.apply(bytes);

    }

    /**
     * Gets the label from the PEM. Thsi should be
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Finds the {@link Rfc7468Label} associated with this PemDecoder.
     *
     * @return a {@link Optional} containing the label
     */
    public Optional<Rfc7468Label> findRfc7468Label() {
        return Rfc7468Label.findForLabel(label);
    }

    /**
     * Gets the spec generated from the key.
     *
     * @return the spec
     */
    public KeySpecT getSpec() {
        return spec;
    }

    /**
     * Returns a human-readable version of this {@link PemDecoder}, redacting the key information.
     *
     * @return a string representing this instance.
     */
    public String toString() {
        return
            HEADER + label + SUFFIX + "\n" +
            "<redacted>" + "\n" +
            FOOTER + label + SUFFIX + "\n";
    }

    /**
     * Validates a PEM file when passed
     */
    public static class Validator implements ConstraintValidator<PemFile, String> {

        @Override
        public void initialize(final PemFile constraintAnnotation) {}

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext context) {
            try {
                final PemDecoder<KeySpec> pemDecoder = new PemDecoder<>(value, dummy -> new KeySpec(){});
                logger.trace("Successfully decoded pem {} {}", pemDecoder.getLabel(), pemDecoder);
                return true;
            } catch (InvalidPemException e) {
                context.buildConstraintViolationWithTemplate("Invalid PEM File.");
                return false;
            }
        }

    }

}
