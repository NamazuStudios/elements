package dev.getelements.elements.rest.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import java.math.BigInteger;
import java.net.URI;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Set;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Full-flow integration test for the OIDC provider (135a), driving real HTTP requests against the embedded
 * REST surface the way a downstream relying party would: discovery, JWKS fetch, the browser-redirect
 * authorization code flow with PKCE against the hosted login page, token exchange, userinfo, refresh
 * rotation, and end-session revocation.
 */
public class OidcProviderApiTest {

    /**
     * The RFC 7636 Appendix B PKCE pair, so the challenge in this test is a known-good value.
     */
    private static final String PKCE_VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
    private static final String PKCE_CHALLENGE = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

    private static final String REDIRECT_URI = "https://rp.example.com/callback";
    private static final String STATE = "rp-state-123";
    private static final String NONCE = "rp-nonce-456";

    @Factory
    public Object[] getTests() {
        return new Object[]{TestUtils.getInstance().getTestFixture(OidcProviderApiTest.class)};
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String rpUsername;

    private String rpUserId;

    private String clientId;

    private String clientSecret;

    private String authorizationId;

    private String code;

    private String idToken;

    private String accessToken;

    private String refreshToken;

    @BeforeClass
    public void setup() {

        // Superuser drives the client-registration surface; the relying party's user is a plain user whose
        // credentials the hosted login page receives.

        // NOTE: ClientContext.createUser()/createSuperuser() clear the sessionCreation in their finally
        // blocks, so the session must be created AFTER the last user is created.
        rpUsername = clientContext.createUser("oidc-provider-rp-user").getUser().getName();
        rpUserId = clientContext.getUser().getId();
        clientContext.createSuperuser("oidc-provider-superuser").createSession();

    }

    @Test
    public void createClientRegistration_returnsSecretOnce() {

        final var registration = new OidcClientRegistration();
        registration.setDisplayName("RP integration client");
        registration.setRedirectUris(Set.of(REDIRECT_URI));
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.AUTHORIZATION_CODE,
                OidcClientRegistration.GrantType.REFRESH_TOKEN));
        registration.setScopes(Set.of("openid", "profile", "email"));

        final var created = client
                .target(apiRoot + "/oidc_client")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(registration, APPLICATION_JSON));

        assertEquals(created.getStatus(), 200);
        final var body = created.readEntity(OidcClientRegistration.class);

        assertNotNull(body.getClientId());
        assertNotNull(body.getClientSecret(), "The raw secret must be echoed on create.");

        clientId = body.getClientId();
        clientSecret = body.getClientSecret();

        // The secret never round-trips on read.
        final var fetched = client
                .target(apiRoot + "/oidc_client/" + clientId)
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get();

        assertEquals(fetched.getStatus(), 200);
        assertEquals(fetched.readEntity(OidcClientRegistration.class).getClientSecret(), null);

    }

    @Test(dependsOnMethods = "createClientRegistration_returnsSecretOnce")
    public void discovery_documentMatchesTokenIssuer() throws Exception {

        final var discovery = client
                .target(apiRoot + "/.well-known/openid-configuration")
                .request(APPLICATION_JSON)
                .get();

        assertEquals(discovery.getStatus(), 200);
        final var document = objectMapper.readTree(discovery.readEntity(String.class));

        assertEquals(document.get("issuer") == null ? "<missing>" : document.get("issuer").asText(),
                apiRoot, "Discovery body was: " + document);
        assertEquals(document.get("token_endpoint").asText(), apiRoot + "/oidc/token");
        assertEquals(document.get("jwks_uri").asText(), apiRoot + "/.well-known/jwks.json");

    }

    @Test(dependsOnMethods = "discovery_documentMatchesTokenIssuer")
    public void authorize_redirectsToHostedLogin() {

        final Response response = client
                .target(apiRoot + "/oidc/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("scope", "openid profile")
                .queryParam("state", STATE)
                .queryParam("nonce", NONCE)
                .queryParam("code_challenge", PKCE_CHALLENGE)
                .queryParam("code_challenge_method", "S256")
                .request()
                .property(FOLLOW_REDIRECTS, false)
                .get();

        assertEquals(response.getStatus(), 303);
        final var location = response.getLocation();
        assertNotNull(location);
        assertTrue(location.getPath().endsWith("/oidc/login"));

        authorizationId = splitQuery(location).get("authorizationId");
        assertNotNull(authorizationId);

    }

    @Test(dependsOnMethods = "authorize_redirectsToHostedLogin")
    public void login_redirectsToRelyingPartyWithCodeAndState() {

        final var form = new Form();
        form.param("authorizationId", authorizationId);
        form.param("username", rpUsername);
        form.param("password", ClientContext.DUMMY_PASSWORD);

        final Response response = client
                .target(apiRoot + "/oidc/login")
                .request()
                .property(FOLLOW_REDIRECTS, false)
                .post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

        assertEquals(response.getStatus(), 303);
        final var location = response.getLocation();
        assertNotNull(location);
        assertTrue(location.toString().startsWith(REDIRECT_URI));

        final var params = splitQuery(location);
        assertNotNull(params.get("code"));
        assertEquals(params.get("state"), STATE);

        code = params.get("code");

    }

    @Test(dependsOnMethods = "login_redirectsToRelyingPartyWithCodeAndState")
    public void exchange_returnsTokensVerifiableAgainstPublishedJwks() throws Exception {

        final Response response = exchangeCode();

        assertEquals(response.getStatus(), 200);
        final var body = response.readEntity(JsonNode.class);

        accessToken = body.get("access_token").asText();
        idToken = body.get("id_token").asText();
        refreshToken = body.get("refresh_token").asText();
        assertEquals(body.get("token_type").asText(), "Bearer");
        assertTrue(body.get("expires_in").asLong() > 0);

        // A downstream RP verifies the id_token against the JWKS it fetched over HTTP — exactly this.

        final var jwks = client
                .target(apiRoot + "/.well-known/jwks.json")
                .request(APPLICATION_JSON)
                .get()
                .readEntity(JsonNode.class);

        final var kid = SignedJWT.parse(idToken).getHeader().getKeyID();
        final var publicKey = publicKeyFromJwks(jwks, kid);

        final var parsed = SignedJWT.parse(idToken);
        assertTrue(parsed.verify(new RSASSAVerifier(publicKey)));
        assertEquals(parsed.getJWTClaimsSet().getAudience().get(0), clientId);
        assertEquals(parsed.getJWTClaimsSet().getStringClaim("azp"), clientId);
        assertEquals(parsed.getJWTClaimsSet().getStringClaim("nonce"), NONCE);

        // The iss claim must match the discovery document's issuer — cross-checked in
        // discovery_documentMatchesTokenIssuer.

    }

    @Test(dependsOnMethods = "exchange_returnsTokensVerifiableAgainstPublishedJwks")
    public void accessToken_authenticatesAgainstElementsApi() {

        // The provider's access token IS an Elements session token: the downstream caller can hit Elements
        // APIs with it directly.

        final Response response = client
                .target(apiRoot + "/user/me")
                .request(APPLICATION_JSON)
                .property(FOLLOW_REDIRECTS, false)
                .header("Authorization", "Bearer " + accessToken)
                .get();

        assertEquals(response.getStatus(), 200);
        assertEquals(response.readEntity(User.class).getName(), rpUsername);

    }

    @Test(dependsOnMethods = "exchange_returnsTokensVerifiableAgainstPublishedJwks")
    public void userinfo_returnsScopeGatedClaims() {

        final Response response = client
                .target(apiRoot + "/oidc/userinfo")
                .request(APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .get();

        assertEquals(response.getStatus(), 200);
        final var claims = response.readEntity(JsonNode.class);

        assertEquals(claims.get("sub").asText(), rpUserId);
        assertNotNull(claims.get("preferred_username"));
        assertFalse(claims.hasNonNull("email"), "email requires the email scope, which was not requested.");

    }

    @Test(dependsOnMethods = "userinfo_returnsScopeGatedClaims")
    public void refresh_rotatesAndRevokesTheOldSession() {

        final var form = new Form();
        form.param("grant_type", "refresh_token");
        form.param("refresh_token", refreshToken);
        form.param("client_id", clientId);
        form.param("client_secret", clientSecret);

        final Response response = client
                .target(apiRoot + "/oidc/token")
                .request(APPLICATION_JSON)
                .post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

        assertEquals(response.getStatus(), 200);
        final var body = response.readEntity(JsonNode.class);

        final var newAccessToken = body.get("access_token").asText();
        assertNotEquals(newAccessToken, accessToken);

        // The old access token's backing session was revoked by the rotation.
        final var oldUserinfo = client
                .target(apiRoot + "/oidc/userinfo")
                .request(APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .get();
        assertEquals(oldUserinfo.getStatus(), 401);

        final var newUserinfo = client
                .target(apiRoot + "/oidc/userinfo")
                .request(APPLICATION_JSON)
                .header("Authorization", "Bearer " + newAccessToken)
                .get();
        assertEquals(newUserinfo.getStatus(), 200);

        idToken = body.get("id_token").asText();
        accessToken = newAccessToken;
        refreshToken = body.get("refresh_token").asText();

    }

    @Test(dependsOnMethods = "refresh_rotatesAndRevokesTheOldSession")
    public void endSession_revokesAllTokensForTheClient() {

        final Response response = client
                .target(apiRoot + "/oidc/end-session")
                .queryParam("id_token_hint", idToken)
                .request()
                .get();

        assertEquals(response.getStatus(), 200);

        final var userinfo = client
                .target(apiRoot + "/oidc/userinfo")
                .request(APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .get();

        assertEquals(userinfo.getStatus(), 401);

    }

    @Test
    public void authorize_unknownClient_isRejected() {

        final Response response = client
                .target(apiRoot + "/oidc/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "el_unknown")
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("scope", "openid")
                .queryParam("code_challenge", PKCE_CHALLENGE)
                .queryParam("code_challenge_method", "S256")
                .request()
                .property(FOLLOW_REDIRECTS, false)
                .get();

        assertEquals(response.getStatus(), 400);

    }

    @Test
    public void createClientRegistration_unauthenticated_isRejected() {

        final var registration = new OidcClientRegistration();
        registration.setDisplayName("Should not be creatable");
        registration.setRedirectUris(Set.of(REDIRECT_URI));
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.AUTHORIZATION_CODE));
        registration.setScopes(Set.of("openid"));

        final Response response = client
                .target(apiRoot + "/oidc_client")
                .request(APPLICATION_JSON)
                .post(Entity.entity(registration, APPLICATION_JSON));

        assertFalse(response.getStatus() == 200);

    }

    @Test(dependsOnMethods = "createClientRegistration_returnsSecretOnce")
    public void exchange_replayedCode_isRejected() throws Exception {

        // A fresh, complete authorization: begin + login.

        final var authorize = client
                .target(apiRoot + "/oidc/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("scope", "openid")
                .queryParam("code_challenge", PKCE_CHALLENGE)
                .queryParam("code_challenge_method", "S256")
                .request()
                .property(FOLLOW_REDIRECTS, false)
                .get();

        final var loginId = splitQuery(authorize.getLocation()).get("authorizationId");

        final var loginForm = new Form();
        loginForm.param("authorizationId", loginId);
        loginForm.param("username", rpUsername);
        loginForm.param("password", ClientContext.DUMMY_PASSWORD);

        final var login = client
                .target(apiRoot + "/oidc/login")
                .request()
                .property(FOLLOW_REDIRECTS, false)
                .post(Entity.entity(loginForm, APPLICATION_FORM_URLENCODED));

        final var replayedCode = splitQuery(login.getLocation()).get("code");

        // First redemption succeeds...
        assertEquals(exchangeRaw(replayedCode, PKCE_VERIFIER).getStatus(), 200);

        // ...the replay fails.
        assertEquals(exchangeRaw(replayedCode, PKCE_VERIFIER).getStatus(), 400);

    }

    private Response exchangeCode() {
        return exchangeRaw(code, PKCE_VERIFIER);
    }

    private Response exchangeRaw(final String codeValue, final String verifier) {

        final var form = new Form();
        form.param("grant_type", "authorization_code");
        form.param("code", codeValue);
        form.param("redirect_uri", REDIRECT_URI);
        form.param("code_verifier", verifier);
        form.param("client_id", clientId);
        form.param("client_secret", clientSecret);

        return client
                .target(apiRoot + "/oidc/token")
                .request(APPLICATION_JSON)
                .post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

    }

    private java.util.Map<String, String> splitQuery(final URI uri) {

        final var params = new java.util.HashMap<String, String>();
        final var rawQuery = uri.getRawQuery();

        if (rawQuery == null) {
            return params;
        }

        for (final var pair : rawQuery.split("&")) {
            final var eq = pair.indexOf('=');
            params.put(
                    pair.substring(0, eq).replace("%20", " "),
                    java.net.URLDecoder.decode(pair.substring(eq + 1), java.nio.charset.StandardCharsets.UTF_8));
        }

        return params;

    }

    private java.security.interfaces.RSAPublicKey publicKeyFromJwks(final JsonNode jwks, final String kid)
            throws Exception {

        for (final var key : jwks.get("keys")) {
            if (kid.equals(key.get("kid").asText())) {
                final var modulus = new BigInteger(1, Base64.getUrlDecoder().decode(key.get("n").asText()));
                final var exponent = new BigInteger(1, Base64.getUrlDecoder().decode(key.get("e").asText()));
                return (java.security.interfaces.RSAPublicKey) KeyFactory
                        .getInstance("RSA")
                        .generatePublic(new RSAPublicKeySpec(modulus, exponent));
            }
        }

        throw new AssertionError("No JWK for kid " + kid);

    }

}
