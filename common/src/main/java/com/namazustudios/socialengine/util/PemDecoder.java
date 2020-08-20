package com.namazustudios.socialengine.util;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.InvalidPemException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.function.Function;

public class PemDecoder<KeySpecT extends KeySpec> {

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

}
