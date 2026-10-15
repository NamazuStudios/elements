package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.dao.JwtSigningKeyDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.auth.JwtSigningKey;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import dev.getelements.elements.sdk.model.exception.security.SessionExpiredException;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.SessionTokenVerifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class StandardSessionTokenIssuerTest {

    private StandardSessionTokenIssuer standardSessionTokenIssuer;

    private StandardSessionTokenKeySupport standardSessionTokenKeySupport;

    private SessionDao sessionDao;

    private JwtSigningKeyDao jwtSigningKeyDao;

    private JwtSigningKey signingKey;

    private RSAPublicKey publicKey;

    @BeforeMethod
    public void setUp() throws Exception {

        sessionDao = mock(SessionDao.class);
        jwtSigningKeyDao = mock(JwtSigningKeyDao.class);

        final var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();

        publicKey = (RSAPublicKey) keyPair.getPublic();

        final var keyFactory = KeyFactory.getInstance("RSA");
        final var publicKeyBase64 = Base64.getEncoder().encodeToString(
                keyFactory.getKeySpec(keyPair.getPublic(), X509EncodedKeySpec.class).getEncoded());
        final var privateKeyBase64 = Base64.getEncoder().encodeToString(
                keyFactory.getKeySpec(keyPair.getPrivate(), java.security.spec.PKCS8EncodedKeySpec.class).getEncoded());

        signingKey = new JwtSigningKey();
        signingKey.setKid("test-kid");
        signingKey.setAlgorithm(JwtSigningKey.ALGORITHM);
        signingKey.setStatus(JwtSigningKey.Status.ACTIVE);
        signingKey.setPublicKey(publicKeyBase64);
        signingKey.setPrivateKey(privateKeyBase64);
        signingKey.setCreatedAt(currentTimeMillis());

        // The issuer decrypts the signing key's private half using the stored encryption metadata,
        // so the fixture key is encrypted here through the same routine used at bootstrap time.

        standardSessionTokenKeySupport = new StandardSessionTokenKeySupport();
        standardSessionTokenKeySupport.setJwtSigningKeyDao(jwtSigningKeyDao);
        standardSessionTokenKeySupport.setPassphrase("test-passphrase");
        standardSessionTokenKeySupport.setKeyRefreshSeconds(300);

        standardSessionTokenKeySupport.encryptPrivateKey(signingKey, privateKeyBase64);

        standardSessionTokenIssuer = new StandardSessionTokenIssuer();
        standardSessionTokenIssuer.setSessionDao(sessionDao);
        standardSessionTokenIssuer.setSessionTokenKeySupport(standardSessionTokenKeySupport);
        standardSessionTokenIssuer.setIssuer("test-issuer");
        standardSessionTokenIssuer.setAudience("test-audience");

    }

    private Session sessionExpiringAt(final long expiry) {

        final var user = new User();
        user.setId("test-user-id");
        user.setLevel(User.Level.USER);

        final var session = new Session();
        session.setUser(user);
        session.setExpiry(expiry);

        return session;

    }

    private SessionCreation sessionCreationFor(final Session session) {
        final var sessionCreation = new SessionCreation();
        sessionCreation.setSessionSecret("test-session-id");
        sessionCreation.setSession(session);
        when(sessionDao.create(any())).thenReturn(sessionCreation);
        return sessionCreation;
    }

    @Test
    public void testIssueSignsTokenWithExpectedClaims() throws Exception {

        when(jwtSigningKeyDao.getSigningKeys(JwtSigningKey.Status.ACTIVE)).thenReturn(List.of(signingKey));
        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of(signingKey));

        final var session = sessionExpiringAt(currentTimeMillis() + SECONDS.toMillis(300));
        sessionCreationFor(session);

        final var issued = standardSessionTokenIssuer.issue(session);

        assertNotNull(issued);
        assertNotNull(issued.getSessionSecret());
        assertNotEquals(issued.getSessionSecret(), "test-session-id",
                "The signed token must replace the raw session id.");

        final var signedJwt = com.nimbusds.jwt.SignedJWT.parse(issued.getSessionSecret());

        assertEquals(signedJwt.getHeader().getKeyID(), "test-kid");
        assertEquals(signedJwt.getHeader().getAlgorithm(), com.nimbusds.jose.JWSAlgorithm.RS256);

        final var claims = signedJwt.getJWTClaimsSet();
        assertEquals(claims.getSubject(), "test-user-id");
        assertEquals(claims.getIssuer(), "test-issuer");
        assertTrue(claims.getAudience().contains("test-audience"));
        assertEquals(claims.getStringClaim("sid"), "test-session-id");
        assertEquals(claims.getStringClaim("elm_lvl"), "USER");
        assertEquals(claims.getExpirationTime().getTime(), session.getExpiry() / 1000 * 1000);

        assertTrue(signedJwt.verify(new com.nimbusds.jose.crypto.RSASSAVerifier(publicKey)),
                "Token must verify with the signing key.");

    }

    @Test
    public void testIssueBootstrapsSigningKeyWhenNoneActive() {

        when(jwtSigningKeyDao.getSigningKeys(JwtSigningKey.Status.ACTIVE)).thenReturn(List.of());
        when(jwtSigningKeyDao.createSigningKey(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of());

        final var session = sessionExpiringAt(currentTimeMillis() + SECONDS.toMillis(300));
        sessionCreationFor(session);

        final var issued = standardSessionTokenIssuer.issue(session);

        assertNotNull(issued.getSessionSecret());

        verify(jwtSigningKeyDao).createSigningKey(any(JwtSigningKey.class));

    }

    @Test
    public void testVerifierRoundTripsIssuedToken() {

        when(jwtSigningKeyDao.getSigningKeys(JwtSigningKey.Status.ACTIVE)).thenReturn(List.of(signingKey));
        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of(signingKey));

        final var session = sessionExpiringAt(currentTimeMillis() + SECONDS.toMillis(300));
        sessionCreationFor(session);

        final var issued = standardSessionTokenIssuer.issue(session);

        final SessionTokenVerifier verifier = newVerifier();

        assertTrue(verifier.isSessionToken(issued.getSessionSecret()),
                "An issued token must be recognized as a session token.");
        assertFalse(verifier.isSessionToken("0123456789abcdef0123456789abcdef"),
                "A legacy opaque secret must not be recognized as a session token.");
        assertFalse(verifier.isSessionToken("a.b"), "Not enough segments.");
        assertFalse(verifier.isSessionToken("a.b.c.d"), "Too many segments.");

        final var claims = verifier.verify(issued.getSessionSecret());

        assertEquals(claims.getSubject(), "test-user-id");
        assertEquals(claims.getSessionId(), "test-session-id");
        assertEquals(claims.getLevel(), "USER");
        assertEquals(claims.getIssuer(), "test-issuer");
        assertEquals(claims.getExpiry(), session.getExpiry() / 1000 * 1000);

    }

    @Test
    public void testVerifierRejectsTamperedToken() {

        when(jwtSigningKeyDao.getSigningKeys(JwtSigningKey.Status.ACTIVE)).thenReturn(List.of(signingKey));
        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of(signingKey));

        final var session = sessionExpiringAt(currentTimeMillis() + SECONDS.toMillis(300));
        sessionCreationFor(session);

        final var token = standardSessionTokenIssuer.issue(session).getSessionSecret();

        // Flip a bit in the payload segment so that the signature no longer matches.

        final var parts = token.split("\\.");
        final var payload = Base64.getUrlDecoder().decode(parts[1]);
        payload[0] = (byte) (payload[0] ^ 0x01);
        final var tampered = parts[0] + "." +
                Base64.getUrlEncoder().withoutPadding().encodeToString(payload) + "." + parts[2];

        final SessionTokenVerifier verifier = newVerifier();

        final var ex = expectThrows(UnauthorizedException.class, () -> verifier.verify(tampered));
        assertNotNull(ex);

    }

    @Test
    public void testVerifierRejectsExpiredToken() {

        when(jwtSigningKeyDao.getSigningKeys(JwtSigningKey.Status.ACTIVE)).thenReturn(List.of(signingKey));
        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of(signingKey));

        final var session = sessionExpiringAt(currentTimeMillis() - 1000);
        sessionCreationFor(session);

        final var token = standardSessionTokenIssuer.issue(session).getSessionSecret();
        final SessionTokenVerifier verifier = newVerifier();

        final var ex = expectThrows(SessionExpiredException.class, () -> verifier.verify(token));
        assertNotNull(ex);

    }

    @Test
    public void testVerifierRejectsWrongIssuer() {

        when(jwtSigningKeyDao.getSigningKeys(JwtSigningKey.Status.ACTIVE)).thenReturn(List.of(signingKey));
        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of(signingKey));

        final var session = sessionExpiringAt(currentTimeMillis() + SECONDS.toMillis(300));
        sessionCreationFor(session);

        final var token = standardSessionTokenIssuer.issue(session).getSessionSecret();

        final var verifier = newVerifier();
        verifier.setIssuer("some-other-issuer");

        final var ex = expectThrows(UnauthorizedException.class, () -> verifier.verify(token));
        assertNotNull(ex);

    }

    @Test
    public void testVerifierRejectsUnknownKid() {

        // The issuer sees the active key, but the verifier's cache refresh resolves no keys, as if
        // the signing key had been retired and removed.

        when(jwtSigningKeyDao.getSigningKeys(JwtSigningKey.Status.ACTIVE)).thenReturn(List.of(signingKey));
        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of());

        final var session = sessionExpiringAt(currentTimeMillis() + SECONDS.toMillis(300));
        sessionCreationFor(session);

        final var token = standardSessionTokenIssuer.issue(session).getSessionSecret();
        final SessionTokenVerifier verifier = newVerifier();

        final var ex = expectThrows(UnauthorizedException.class, () -> verifier.verify(token));
        assertNotNull(ex);

    }

    private StandardSessionTokenVerifier newVerifier() {
        final var verifier = new StandardSessionTokenVerifier();
        verifier.setSessionTokenKeySupport(standardSessionTokenKeySupport);
        verifier.setIssuer("test-issuer");
        verifier.setAudience("test-audience");
        return verifier;
    }

}
