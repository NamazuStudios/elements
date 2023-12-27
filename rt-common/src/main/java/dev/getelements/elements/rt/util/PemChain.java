package dev.getelements.elements.rt.util;

import dev.getelements.elements.rt.exception.InvalidPemException;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.nio.ByteBuffer.wrap;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a chain of key specifications which may be found in a single PEM file.
 */
public class PemChain {

    private final List<PemData<ByteBuffer>> chain;

    /**
     * Constructs a {@link PemChain} from a {@link String}.
     *
     * @param pemString the string containing the PEM Chain File
     * @throws InvalidPemException
     */
    public PemChain(final String pemString) throws InvalidPemException {
        this(new StringReader(pemString));
    }

    /**
     * Constructs a {@link PemChain} from an {@link InputStream}.
     *
     * @param inputStream an {@link InputStream} with the contents of a PEM Chain file.
     * @throws InvalidPemException
     */
    public PemChain(final InputStream inputStream) throws InvalidPemException {
        this(new InputStreamReader(inputStream, US_ASCII));
    }

    /**
     * Constructs a {@link PemChain} from a {@link Reader}.
     *
     * @param reader an {@link Reader} with the contents of a PEM Chain file.
     * @throws InvalidPemException
     */
    public PemChain(final Reader reader) throws InvalidPemException {
        this(new BufferedReader(reader));
    }

    /**
     * Constructs a {@link PemChain} from a {@link BufferedReader}.
     *
     * @param reader an {@link BufferedReader} with the contents of a PEM Chain file.
     * @throws InvalidPemException
     */
    public PemChain(final BufferedReader reader) throws InvalidPemException {
        try {

            final var chain = new ArrayList<PemData<ByteBuffer>>();

            do {
                final var data = new PemData<>(reader, b -> wrap(b).asReadOnlyBuffer());
                chain.add(data);
            } while (hasMore(reader));

            this.chain = unmodifiableList(chain);

        } catch (IOException ex) {
            throw new InvalidPemException(ex);
        }

    }

    private static boolean hasMore(final BufferedReader reader) throws IOException {
        final boolean more;
        reader.mark(1);
        more = reader.read() >= 0;
        reader.reset();
        return more;
    }

    /**
     * Gets the chain of {@link PemData}.
     *
     * @return a {@link List} representing the chain of {@link PemData}
     */
    public List<PemData<ByteBuffer>> getChain() {
        return chain;
    }

    /**
     * Finds the first speck with the supplied label.
     *
     * @param label the label to find
     * @return an {@link Optional} specifying the requested label or null
     */
    public Optional<PemData<ByteBuffer>> findFirstWithLabel(final String label) {
        return getChain()
                .stream()
                .filter(p -> Objects.equals(p.getLabel(), label))
                .findFirst();
    }

    /**
     * Finds the first speck with the supplied {@link Rfc7468Label}.
     *
     * @param label the {@link Rfc7468Label} to find
     * @return an {@link Optional} specifying the requested label or null
     */
    public Optional<PemData<ByteBuffer>> findFirstWithLabel(final Rfc7468Label label) {
        return findFirstWithLabel(label.getLabel());
    }

}
