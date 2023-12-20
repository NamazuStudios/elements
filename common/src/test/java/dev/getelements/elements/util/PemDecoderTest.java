package dev.getelements.elements.util;

import dev.getelements.elements.exception.InvalidPemException;
import org.testng.annotations.Test;

import java.io.IOException;

import static dev.getelements.elements.util.Rfc7468Label.PRIVATE_KEY;
import static dev.getelements.elements.util.Rfc7468Label.PUBLIC_KEY;
import static org.testng.Assert.assertEquals;

public class PemDecoderTest {

    @Test
    public void testReadPublicKey() throws IOException, InvalidPemException {
        try (final var input = PemDecoderTest.class.getResourceAsStream("/public.pem")) {
            final var decoder = new PemDecoder<>(input, bytes -> bytes);
            assertEquals(decoder.getLabel(), "PUBLIC KEY");
            assertEquals(decoder.findRfc7468Label().get(), PUBLIC_KEY);
        }
    }

    @Test
    public void testReadPrivateKey() throws IOException, InvalidPemException {
        try (final var input = PemDecoderTest.class.getResourceAsStream("/private.pem")) {
            final var decoder = new PemDecoder<>(input, bytes -> bytes);
            assertEquals(decoder.getLabel(), "PRIVATE KEY");
            assertEquals(decoder.findRfc7468Label().get(), PRIVATE_KEY);
        }
    }

}
