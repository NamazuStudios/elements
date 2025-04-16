package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.model.security.InvalidPemException;
import dev.getelements.elements.sdk.model.security.PemData;
import org.testng.annotations.Test;

import java.io.IOException;

import static dev.getelements.elements.sdk.model.security.Rfc7468Label.PRIVATE_KEY;
import static dev.getelements.elements.sdk.model.security.Rfc7468Label.PUBLIC_KEY;
import static org.testng.Assert.assertEquals;

public class PemDataTest {

    @Test
    public void testReadPublicKey() throws IOException, InvalidPemException {
        try (final var input = PemDataTest.class.getResourceAsStream("/public.pem")) {
            final var decoder = new PemData<>(input, bytes -> bytes);
            assertEquals(decoder.getLabel(), "PUBLIC KEY");
            assertEquals(decoder.findRfc7468Label().get(), PUBLIC_KEY);
        }
    }

    @Test
    public void testReadPrivateKey() throws IOException, InvalidPemException {
        try (final var input = PemDataTest.class.getResourceAsStream("/private.pem")) {
            final var decoder = new PemData<>(input, bytes -> bytes);
            assertEquals(decoder.getLabel(), "PRIVATE KEY");
            assertEquals(decoder.findRfc7468Label().get(), PRIVATE_KEY);
        }
    }

}
