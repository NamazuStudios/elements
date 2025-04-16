package dev.getelements.elements.sdk.model.security;

import java.io.*;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a section of PEM (Privacy Enhanced Mail) data which may include a section of a key.
 *
 * @param <KeySpecT>
 */
public class PemData<KeySpecT> {

    public static final int MAX_LINE_LENGTH = 64;

    private static final String SUFFIX = "-----";

    private static final String HEADER = "-----BEGIN ";

    private static final String FOOTER = "-----END ";

    private final String label;

    private final KeySpecT spec;

    public PemData(final String pemString,
                   final Function<byte[], KeySpecT> keySpecFunction) throws InvalidPemException {
        this(new StringReader(pemString), keySpecFunction);
    }

    public PemData(final InputStream inputStream,
                   final Function<byte[], KeySpecT> keySpecFunction) throws InvalidPemException {
        this(new InputStreamReader(inputStream, UTF_8), keySpecFunction);
    }

    public PemData(final Reader reader,
                   final Function<byte[], KeySpecT> keySpecFunction) throws InvalidPemException {
        this(new BufferedReader(reader), keySpecFunction);
    }

    public PemData(final BufferedReader reader,
                   final Function<byte[], KeySpecT> keySpecFunction) throws InvalidPemException {

        final StringBuilder encoded = new StringBuilder();

        try {

            final String header = reader.readLine().trim();

            if (!(header.startsWith(HEADER) && header.endsWith(SUFFIX)))
                throw new InvalidPemException("Invalid PEM format. Missing or malformed header.");

            label = header.substring(HEADER.length(), header.length() - SUFFIX.length());

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(FOOTER) && line.endsWith(SUFFIX)) break;
                else if (line.length() > MAX_LINE_LENGTH) throw new InvalidPemException("Line length exceeded.");
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
     * Creates an instance of {@link PemData} with the supplied label and specification.
     * @param label the label
     * @param spec the specification
     */
    public PemData(final String label, final KeySpecT spec) {
        this.label = label;
        this.spec = spec;
    }

    /**
     * Creates an instance of {@link PemData} with the supplied label and specification.
     *
     * @param label the label
     * @param spec the specification
     */
    public PemData(final Rfc7468Label label, final KeySpecT spec) {
        this.label = label.getLabel();
        this.spec = spec;
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
     * Maps this {@link PemData} to another type.
     *
     * @param mapper the mapper function
     * @param <OtherKeySpecT> the other pem spec type
     *
     * @return a new instance of the {@link PemData} with the spec translated to the requested format
     */
    public <OtherKeySpecT> PemData<OtherKeySpecT> map(final Function<KeySpecT, OtherKeySpecT> mapper) {
        return new PemData<>(label, mapper.apply(spec));
    }

    /**
     * Returns a human-readable version of this {@link PemData}, redacting the key information.
     *
     * @return a string representing this instance.
     */
    public String toString() {
        return
            HEADER + label + SUFFIX + "\n" +
            "<redacted>" + "\n" +
            FOOTER + label + SUFFIX + "\n";
    }

}
