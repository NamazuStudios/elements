package dev.getelements.elements.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.service.auth.oidc.OidcAuthServiceOperations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Focused coverage for the aud/nonce/iss checks added to {@code OidcAuthServiceOperations.verify(...)} so the
 * callback path (which supplies both) and the existing direct id_token path (which supplies neither) share
 * validation code. Exercised directly through {@link OidcAuthServiceOperations#decodeAndVerify}, with no Guice
 * injector needed since this path never touches the DAO/Client fields.
 */
public class OidcAuthServiceOperationsValidationTest {

    private static final String ISSUER = "https://issuer.test";
    private static final String CLIENT_ID = "test-client-id";
    private static final String NONCE = "test-nonce-value";
    private static final String KID = "test-kid";

    private RSAPublicKey publicKey;
    private Algorithm algorithm;
    private OidcAuthServiceOperations operations;

    @BeforeClass
    public void setup() throws Exception {

        final var kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        final var kp = kpg.generateKeyPair();

        publicKey = (RSAPublicKey) kp.getPublic();
        algorithm = Algorithm.RSA256(publicKey, (RSAPrivateKey) kp.getPrivate());
        operations = new OidcAuthServiceOperations();

    }

    private OidcAuthScheme scheme() {

        final var n = Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray());
        final var e = Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray());
        final var jwk = new JWK("RS256", KID, "RSA", "sig", e, n);

        final var scheme = new OidcAuthScheme();
        scheme.setIssuer(ISSUER);
        scheme.setKeys(List.of(jwk));

        return scheme;

    }

    private String token(final String issuer, final String audience, final String nonce) {

        var builder = JWT.create()
                .withIssuer(issuer)
                .withSubject("test-subject")
                .withKeyId(KID)
                .withExpiresAt(new Date(currentTimeMillis() + 60_000));

        if (audience != null) {
            builder = builder.withAudience(audience);
        }

        if (nonce != null) {
            builder = builder.withClaim("nonce", nonce);
        }

        return builder.sign(algorithm);

    }

    @Test
    public void testNoExpectedAudienceOrNonceSucceeds() {
        final var decoded = operations.decodeAndVerify(token(ISSUER, null, null), scheme(), null, null);
        assertNotNull(decoded);
    }

    @Test
    public void testMatchingAudienceAndNonceSucceeds() {
        final var decoded = operations.decodeAndVerify(token(ISSUER, CLIENT_ID, NONCE), scheme(), CLIENT_ID, NONCE);
        assertEquals(decoded.getSubject(), "test-subject");
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testAudienceMismatchRejected() {
        operations.decodeAndVerify(token(ISSUER, "some-other-client", NONCE), scheme(), CLIENT_ID, NONCE);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testMissingAudienceRejectedWhenExpected() {
        operations.decodeAndVerify(token(ISSUER, null, NONCE), scheme(), CLIENT_ID, NONCE);
    }

    @Test
    public void testAudienceCheckSkippedWhenNotExpected() {
        // A real-world token normally carries an audience, but callers of the direct id_token path (today's
        // Google/Apple schemes) never pass an expected audience, so any (or no) aud claim must be a no-op.
        final var decoded = operations.decodeAndVerify(token(ISSUER, "irrelevant-audience", null), scheme(), null, null);
        assertNotNull(decoded);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testNonceMismatchRejected() {
        operations.decodeAndVerify(token(ISSUER, CLIENT_ID, "wrong-nonce"), scheme(), CLIENT_ID, NONCE);
    }

    @Test
    public void testNonceCheckSkippedWhenNotExpected() {
        final var decoded = operations.decodeAndVerify(token(ISSUER, CLIENT_ID, "any-nonce-at-all"), scheme(), CLIENT_ID, null);
        assertNotNull(decoded);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testIssuerMismatchRejected() {
        operations.decodeAndVerify(token("https://not-the-configured-issuer", null, null), scheme(), null, null);
    }

}
