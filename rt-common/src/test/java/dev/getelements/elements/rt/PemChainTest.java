package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.model.security.InvalidPemException;
import dev.getelements.elements.sdk.model.security.PemChain;
import dev.getelements.elements.sdk.model.security.Rfc7468Label;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class PemChainTest {

    @Test
    public void testReadChain() throws IOException, InvalidPemException {
        try (final var input = PemDataTest.class.getResourceAsStream("/chain.pem")) {
            final var chain = new PemChain(input);
            assertEquals(chain.size(), 2);
            assertNotNull(chain.findFirstWithLabel(Rfc7468Label.PUBLIC_KEY).get());
            assertNotNull(chain.findFirstWithLabel(Rfc7468Label.PRIVATE_KEY).get());
        }
    }

    @Test
    public void testReadPublic() throws IOException, InvalidPemException {
        try (final var input = PemDataTest.class.getResourceAsStream("/public.pem")) {
            final var chain = new PemChain(input);
            assertEquals(chain.size(), 1);
            assertNotNull(chain.findFirstWithLabel(Rfc7468Label.PUBLIC_KEY).get());
        }
    }

    @Test
    public void testReadPrivate() throws IOException, InvalidPemException {
        try (final var input = PemDataTest.class.getResourceAsStream("/private.pem")) {
            final var chain = new PemChain(input);
            assertEquals(chain.size(), 1);
            assertNotNull(chain.findFirstWithLabel(Rfc7468Label.PRIVATE_KEY).get());
        }
    }

}
