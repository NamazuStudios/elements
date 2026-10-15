package dev.getelements.elements.service.auth.oidc.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import dev.getelements.elements.sdk.dao.JwtSigningKeyDao;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.JWKSet;
import dev.getelements.elements.sdk.model.auth.JwtSigningKey;
import dev.getelements.elements.sdk.model.oidc.OidcProviderDiscoveryDocument;
import dev.getelements.elements.sdk.service.Constants;
import dev.getelements.elements.service.auth.StandardSessionTokenKeySupport;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.CODE_CHALLENGE_METHOD_S256;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.GRANT_TYPE_AUTHORIZATION_CODE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.GRANT_TYPE_CLIENT_CREDENTIALS;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.GRANT_TYPE_REFRESH_TOKEN;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.RESPONSE_TYPE_CODE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.SCOPE_EMAIL;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.SCOPE_OPENID;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.SCOPE_PROFILE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the discovery and JWKS services. The discovery document must describe exactly the endpoints
 * the resources expose, resolved against the configured issuer; the JWKS must publish every public signing
 * key (active and retired) so tokens issued before a rotation keep verifying.
 */
public class StandardOidcProviderDiscoveryServiceTest {

    private static final String ISSUER = "https://elements.example.com/api/rest";

    @Inject
    private StandardOidcProviderDiscoveryService discoveryService;

    @Inject
    private StandardOidcProviderJwksService jwksService;

    private JwtSigningKeyDao jwtSigningKeyDao;

    private StandardSessionTokenKeySupport sessionTokenKeySupport;

    private RSAPublicKey publicKey;

    private java.security.interfaces.RSAPrivateKey privateKey;

    @BeforeMethod
    public void setup() throws Exception {

        jwtSigningKeyDao = mock(JwtSigningKeyDao.class);
        sessionTokenKeySupport = mock(StandardSessionTokenKeySupport.class);

        final var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (java.security.interfaces.RSAPrivateKey) keyPair.getPrivate();

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(JwtSigningKeyDao.class).toInstance(jwtSigningKeyDao);
                bind(StandardSessionTokenKeySupport.class).toInstance(sessionTokenKeySupport);
                bind(String.class).annotatedWith(Names.named(Constants.SESSION_TOKEN_KEY_PASSPHRASE))
                        .toInstance("test-passphrase");
                bind(long.class).annotatedWith(Names.named(Constants.SESSION_TOKEN_KEY_REFRESH_SECONDS))
                        .toInstance(300L);
                bind(String.class).annotatedWith(Names.named(Constants.OIDC_PROVIDER_ISSUER)).toInstance(ISSUER);
                bind(StandardOidcProviderDiscoveryService.class);
                bind(StandardOidcProviderJwksService.class);
            }
        }).injectMembers(this);

    }

    private JwtSigningKey signingKey(final String kid, final JwtSigningKey.Status status) {
        final var signingKey = new JwtSigningKey();
        signingKey.setKid(kid);
        signingKey.setAlgorithm(JwtSigningKey.ALGORITHM);
        signingKey.setStatus(status);
        signingKey.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        return signingKey;
    }

    @Test
    public void testDiscoveryDocumentDescribesTheProvider() {

        final var document = discoveryService.getDiscoveryDocument();

        assertEquals(document.getIssuer(), ISSUER);
        assertEquals(document.getAuthorizationEndpoint(), ISSUER + "/oidc/authorize");
        assertEquals(document.getTokenEndpoint(), ISSUER + "/oidc/token");
        assertEquals(document.getUserinfoEndpoint(), ISSUER + "/oidc/userinfo");
        assertEquals(document.getEndSessionEndpoint(), ISSUER + "/oidc/end-session");
        assertEquals(document.getJwksUri(), ISSUER + "/.well-known/jwks.json");

        assertEquals(document.getGrantTypesSupported(),
                Set.of(GRANT_TYPE_AUTHORIZATION_CODE, GRANT_TYPE_REFRESH_TOKEN, GRANT_TYPE_CLIENT_CREDENTIALS));
        assertEquals(document.getScopesSupported(), Set.of(SCOPE_OPENID, SCOPE_PROFILE, SCOPE_EMAIL));
        assertEquals(document.getResponseTypesSupported(), Set.of(RESPONSE_TYPE_CODE));
        assertEquals(document.getCodeChallengeMethodsSupported(), Set.of(CODE_CHALLENGE_METHOD_S256));
        assertEquals(document.getIdTokenSigningAlgValuesSupported(), Set.of("RS256"));

    }

    @Test
    public void testDiscoveryRejectsIssuerWithTrailingSlash() {
        // A trailing slash would make the iss claim and the endpoint URLs inconsistent with the document's
        // location; the discovery specification requires the document at <issuer>/.well-known/...
        discoveryService.setIssuer(ISSUER + "/");
        assertThrows(IllegalStateException.class, discoveryService::getDiscoveryDocument);
    }

    @Test
    public void testJwksPublishesEveryKeyIncludingRetired() throws Exception {

        final var active = signingKey("kid-active", JwtSigningKey.Status.ACTIVE);
        final var retired = signingKey("kid-retired", JwtSigningKey.Status.RETIRED);

        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of(active, retired));
        when(sessionTokenKeySupport.decodePublicKey(active.getPublicKey())).thenReturn(publicKey);
        when(sessionTokenKeySupport.decodePublicKey(retired.getPublicKey())).thenReturn(publicKey);

        final var jwkSet = jwksService.getJwks();
        assertNotNull(jwkSet);
        assertEquals(jwkSet.getKeys().size(), 2);

        // Each published key round-trips to a verifiable RSA JWK: a downstream RP must be able to fetch the
        // JWKS over HTTP and verify tokens without any other key material.

        for (final JWK jwk : jwkSet.getKeys()) {

            assertEquals(jwk.getKty(), "RSA");
            assertEquals(jwk.getUse(), "sig");
            assertEquals(jwk.getAlg(), JwtSigningKey.ALGORITHM);

            // Each published key round-trips to a usable RSA public key: a downstream RP must be able to
            // fetch the JWKS over HTTP and verify tokens with no other key material.

            final var modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getN()));
            final var exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getE()));

            final var reconstructed = (RSAPublicKey) java.security.KeyFactory.getInstance("RSA")
                    .generatePublic(new java.security.spec.RSAPublicKeySpec(modulus, exponent));

            assertEquals(reconstructed, publicKey);

        }

        final var kids = jwkSet.getKeys().stream().map(JWK::getKid).toList();
        assertTrue(kids.contains("kid-active"), "Retired keys stay published: " + kids);
        assertTrue(kids.contains("kid-retired"), "Retired keys stay published: " + kids);

    }

    @Test
    public void testJwksRejectsUndecodableKeyMaterial() {
        final var broken = signingKey("kid-broken", JwtSigningKey.Status.ACTIVE);
        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of(broken));
        when(sessionTokenKeySupport.decodePublicKey(broken.getPublicKey()))
                .thenThrow(new IllegalArgumentException("not base64"));
        assertThrows(dev.getelements.elements.sdk.model.exception.InternalException.class,
                jwksService::getJwks);
    }

    /**
     * Sanity check tying the two services together: a token signed under a kid verifies against the public
     * key the JWKS publishes for that kid.
     */
    @Test
    public void testPublishedJwkVerifiesTokensSignedUnderThatKid() throws Exception {

        final var signingKey = signingKey("kid-jwks-roundtrip", JwtSigningKey.Status.ACTIVE);
        when(jwtSigningKeyDao.getSigningKeys()).thenReturn(List.of(signingKey));
        when(sessionTokenKeySupport.decodePublicKey(signingKey.getPublicKey())).thenReturn(publicKey);

        final var jwk = jwksService.getJwks().getKeys().get(0);

        final var modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getN()));
        final var exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getE()));

        final var jwkPublicKey = (RSAPublicKey) java.security.KeyFactory.getInstance("RSA")
                .generatePublic(new java.security.spec.RSAPublicKeySpec(modulus, exponent));

        final var toSign = new SignedJWT(
                new com.nimbusds.jose.JWSHeader.Builder(com.nimbusds.jose.JWSAlgorithm.RS256)
                        .keyID(jwk.getKid()).build(),
                new com.nimbusds.jwt.JWTClaimsSet.Builder().subject("sub").issuer(ISSUER).build());
        toSign.sign(new com.nimbusds.jose.crypto.RSASSASigner(privateKey));

        assertTrue(toSign.verify(new RSASSAVerifier(jwkPublicKey)));

    }

    public StandardOidcProviderDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public StandardOidcProviderJwksService getJwksService() {
        return jwksService;
    }

}
