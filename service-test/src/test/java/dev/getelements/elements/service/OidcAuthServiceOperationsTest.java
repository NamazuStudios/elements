package dev.getelements.elements.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.service.auth.oidc.OidcAuthServiceOperations;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Exercises {@link OidcAuthServiceOperations}'s JWK-to-PublicKey construction and signature-algorithm dispatch
 * directly (no Guice, no DAOs) -- signature verification for the direct-key-match path only depends on the scheme's
 * own {@code keys} list and the JWT itself, so a bare instance is enough.
 */
public class OidcAuthServiceOperationsTest {

    private static final String ISSUER = "https://issuer.example.com";

    private final OidcAuthServiceOperations operations = new OidcAuthServiceOperations();

    private KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        final var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private KeyPair generateEcKeyPair(final String curveName)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        final var generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec(curveName));
        return generator.generateKeyPair();
    }

    private JWK rsaJwk(final String alg, final String kid, final RSAPublicKey publicKey) {
        final var nimbusKey = new RSAKey.Builder(publicKey).build();
        final var jwk = new JWK();
        jwk.setAlg(alg);
        jwk.setKid(kid);
        jwk.setKty(nimbusKey.getKeyType().getValue());
        jwk.setUse("sig");
        jwk.setN(nimbusKey.getModulus().toString());
        jwk.setE(nimbusKey.getPublicExponent().toString());
        return jwk;
    }

    private JWK ecJwk(final String alg, final String kid, final Curve curve, final ECPublicKey publicKey) {
        final var nimbusKey = new ECKey.Builder(curve, publicKey).build();
        final var jwk = new JWK();
        jwk.setAlg(alg);
        jwk.setKid(kid);
        jwk.setKty(nimbusKey.getKeyType().getValue());
        jwk.setUse("sig");
        jwk.setCrv(curve.getName());
        jwk.setX(nimbusKey.getX().toString());
        jwk.setY(nimbusKey.getY().toString());
        return jwk;
    }

    private OidcAuthScheme schemeWithKeys(final JWK... keys) {
        final var scheme = new OidcAuthScheme();
        scheme.setIssuer(ISSUER);
        scheme.setKeys(List.of(keys));
        return scheme;
    }

    private String signedToken(final Algorithm algorithm, final String kid) {
        return JWT.create()
                .withKeyId(kid)
                .withIssuer(ISSUER)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(algorithm);
    }

    @DataProvider
    public Object[][] rsaAlgorithms() {
        return new Object[][]{
                {"RS256"},
                {"RS384"},
                {"RS512"},
        };
    }

    @Test(dataProvider = "rsaAlgorithms")
    public void verifiesRsaTokenForEveryAlg(final String alg) throws Exception {

        final var keyPair = generateRsaKeyPair();
        final var publicKey = (RSAPublicKey) keyPair.getPublic();
        final var kid = UUID.randomUUID().toString();
        final var jwk = rsaJwk(alg, kid, publicKey);
        final var scheme = schemeWithKeys(jwk);

        final var algorithm = switch (alg) {
            case "RS256" -> Algorithm.RSA256(publicKey, (java.security.interfaces.RSAPrivateKey) keyPair.getPrivate());
            case "RS384" -> Algorithm.RSA384(publicKey, (java.security.interfaces.RSAPrivateKey) keyPair.getPrivate());
            case "RS512" -> Algorithm.RSA512(publicKey, (java.security.interfaces.RSAPrivateKey) keyPair.getPrivate());
            default -> throw new IllegalStateException(alg);
        };

        final var token = signedToken(algorithm, kid);
        final var decoded = operations.decodeAndVerify(token, scheme, null, null);
        assertNotNull(decoded);

    }

    @DataProvider
    public Object[][] ecCurves() {
        return new Object[][]{
                {"ES256", Curve.P_256, "secp256r1"},
                {"ES384", Curve.P_384, "secp384r1"},
        };
    }

    @Test(dataProvider = "ecCurves")
    public void verifiesEcTokenForEveryCurve(final String alg, final Curve curve, final String stdCurveName)
            throws Exception {

        final var keyPair = generateEcKeyPair(stdCurveName);
        final var publicKey = (ECPublicKey) keyPair.getPublic();
        final var kid = UUID.randomUUID().toString();
        final var jwk = ecJwk(alg, kid, curve, publicKey);
        final var scheme = schemeWithKeys(jwk);

        final var algorithm = switch (alg) {
            case "ES256" -> Algorithm.ECDSA256(publicKey, (java.security.interfaces.ECPrivateKey) keyPair.getPrivate());
            case "ES384" -> Algorithm.ECDSA384(publicKey, (java.security.interfaces.ECPrivateKey) keyPair.getPrivate());
            default -> throw new IllegalStateException(alg);
        };

        final var token = signedToken(algorithm, kid);
        final var decoded = operations.decodeAndVerify(token, scheme, null, null);
        assertNotNull(decoded);

    }

    @Test(expectedExceptions = InternalException.class)
    public void rejectsUnsupportedAlg() throws Exception {

        final var keyPair = generateRsaKeyPair();
        final var publicKey = (RSAPublicKey) keyPair.getPublic();
        final var kid = UUID.randomUUID().toString();
        final var jwk = rsaJwk("RS999", kid, publicKey);
        final var scheme = schemeWithKeys(jwk);

        final var algorithm = Algorithm.RSA256(publicKey, (java.security.interfaces.RSAPrivateKey) keyPair.getPrivate());
        final var token = signedToken(algorithm, kid);

        operations.decodeAndVerify(token, scheme, null, null);

    }

    /**
     * Regression test for a signature-verification bypass: the direct-key-match path used to discard
     * {@code attemptVerify}'s return value, so a token whose "kid" header matched a cached key -- a public,
     * non-secret value -- would pass verification even with a bad signature (e.g. signed by an attacker-controlled
     * key rather than the real one on file for that kid).
     */
    @Test
    public void rejectsTokenSignedWithWrongKeyEvenWhenKidMatches() throws Exception {

        final var realKeyPair = generateRsaKeyPair();
        final var forgedKeyPair = generateRsaKeyPair();

        final var kid = UUID.randomUUID().toString();
        final var jwk = rsaJwk("RS256", kid, (RSAPublicKey) realKeyPair.getPublic());
        final var scheme = schemeWithKeys(jwk);

        // Signed with a different key than the one on file for this kid.
        final var forgedAlgorithm = Algorithm.RSA256(
                (RSAPublicKey) forgedKeyPair.getPublic(),
                (java.security.interfaces.RSAPrivateKey) forgedKeyPair.getPrivate()
        );
        final var forgedToken = signedToken(forgedAlgorithm, kid);

        try {
            operations.decodeAndVerify(forgedToken, scheme, null, null);
            fail("Expected verification of a token signed with the wrong key to be rejected");
        } catch (ForbiddenException expected) {
            // expected
        }

    }

}
