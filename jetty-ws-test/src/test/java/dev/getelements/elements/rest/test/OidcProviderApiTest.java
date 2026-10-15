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

    /** A dedicated machine client exercising the client_credentials grant. */
    private String machineClientId;

    private String machineClientSecret;

    /** A dedicated client used to drive the token endpoint into its 429 path. */
    private String rateLimitClientId;

    private String rateLimitClientSecret;

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
    public void login_presentsConsentScreen() {

        final var form = new Form();
        form.param("authorizationId", authorizationId);
        form.param("username", rpUsername);
        form.param("password", ClientContext.DUMMY_PASSWORD);

        final Response response = client
                .target(apiRoot + "/oidc/login")
                .request()
                .property(FOLLOW_REDIRECTS, false)
                .post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

        // The deployment requires explicit consent: the login submission is answered with the hosted
        // consent screen rather than an immediate redirect.

        assertEquals(response.getStatus(), 200);
        final var html = response.readEntity(String.class);

        assertTrue(html.contains("Grant access"), "Expected the consent screen: " + html);
        assertTrue(html.contains("RP integration client"), "The consent screen names the client: " + html);
        assertTrue(html.contains("openid") && html.contains("profile"),
                "The consent screen lists the requested scopes: " + html);

        // The consent form carries the pending authorization's id in its hidden field.
        final var consentAuthorizationId = hiddenAuthorizationId(html);
        assertEquals(consentAuthorizationId, authorizationId);

    }

    @Test(dependsOnMethods = "login_presentsConsentScreen")
    public void consentApproval_redirectsToRelyingPartyWithCodeAndState() {

        final var form = new Form();
        form.param("authorizationId", authorizationId);
        form.param("decision", "approve");

        final Response response = client
                .target(apiRoot + "/oidc/consent")
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

    @Test(dependsOnMethods = "consentApproval_redirectsToRelyingPartyWithCodeAndState")
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

        // A fresh, complete authorization: begin + login + consent approval.

        final var location = java.net.URI.create(completeNewAuthorization("openid", "approve"));
        final var replayedCode = splitQuery(location).get("code");

        // First redemption succeeds...
        assertEquals(exchangeRaw(replayedCode, PKCE_VERIFIER).getStatus(), 200);

        // ...the replay fails.
        assertEquals(exchangeRaw(replayedCode, PKCE_VERIFIER).getStatus(), 400);

    }

    @Test(dependsOnMethods = "createClientRegistration_returnsSecretOnce")
    public void consentDenied_redirectsWithAccessDenied() throws Exception {

        final var location = java.net.URI.create(completeNewAuthorization("openid", "deny"));

        assertTrue(location.toString().startsWith(REDIRECT_URI));
        final var params = splitQuery(location);
        assertEquals(params.get("error"), "access_denied");
        assertEquals(params.get("state"), STATE);

    }

    @Test(dependsOnMethods = "createClientRegistration_returnsSecretOnce")
    public void tokenEndpoint_errorsUseOAuthErrorBodies() throws Exception {

        // Bad client credentials: 401 with the RFC 6749 error shape.

        final var badSecretForm = new Form();
        badSecretForm.param("grant_type", "authorization_code");
        badSecretForm.param("code", "anything");
        badSecretForm.param("redirect_uri", REDIRECT_URI);
        badSecretForm.param("code_verifier", PKCE_VERIFIER);
        badSecretForm.param("client_id", clientId);
        badSecretForm.param("client_secret", "wrong-secret");

        final var badSecret = client
                .target(apiRoot + "/oidc/token")
                .request(APPLICATION_JSON)
                .post(Entity.entity(badSecretForm, APPLICATION_FORM_URLENCODED));

        assertEquals(badSecret.getStatus(), 401);
        final var badSecretBody = objectMapper.readTree(badSecret.readEntity(String.class));
        assertEquals(badSecretBody.get("error").asText(), "invalid_client");
        assertNotNull(badSecretBody.get("error_description"));

        // A bad grant_type: unsupported_grant_type.

        final var badGrantForm = new Form();
        badGrantForm.param("grant_type", "password");
        badGrantForm.param("client_id", clientId);
        badGrantForm.param("client_secret", clientSecret);

        final var badGrant = client
                .target(apiRoot + "/oidc/token")
                .request(APPLICATION_JSON)
                .post(Entity.entity(badGrantForm, APPLICATION_FORM_URLENCODED));

        assertEquals(badGrant.getStatus(), 400);
        assertEquals(objectMapper.readTree(badGrant.readEntity(String.class)).get("error").asText(),
                "unsupported_grant_type");

        // A bogus code: invalid_grant.

        final var badCode = exchangeRaw("bogus-code", PKCE_VERIFIER);
        assertEquals(badCode.getStatus(), 400);
        assertEquals(objectMapper.readTree(badCode.readEntity(String.class)).get("error").asText(),
                "invalid_grant");

    }

    @Test(dependsOnMethods = "createClientRegistration_returnsSecretOnce")
    public void clientCredentials_issuesMachineTokenVerifiableAgainstJwks() throws Exception {

        final var created = registerMachineClient();
        machineClientId = created.getClientId();
        machineClientSecret = created.getClientSecret();

        final var form = new Form();
        form.param("grant_type", "client_credentials");
        form.param("client_id", machineClientId);
        form.param("client_secret", machineClientSecret);
        form.param("audience", "filebrowser");
        form.param("scope", "openid profile");

        final Response response = client
                .target(apiRoot + "/oidc/token")
                .request(APPLICATION_JSON)
                .post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

        assertEquals(response.getStatus(), 200);
        final var body = response.readEntity(JsonNode.class);

        // No end-user is involved: no id_token, no refresh token, no grant — the access token is a
        // standalone JWT the downstream service verifies against the JWKS.

        assertFalse(body.hasNonNull("id_token"));
        assertFalse(body.hasNonNull("refresh_token"));
        assertEquals(body.get("token_type").asText(), "Bearer");
        assertTrue(body.get("expires_in").asLong() > 0 && body.get("expires_in").asLong() <= 600);

        final var machineToken = body.get("access_token").asText();

        final var jwks = client
                .target(apiRoot + "/.well-known/jwks.json")
                .request(APPLICATION_JSON)
                .get()
                .readEntity(JsonNode.class);

        final var parsed = SignedJWT.parse(machineToken);
        final var kid = parsed.getHeader().getKeyID();
        assertTrue(parsed.verify(new RSASSAVerifier(publicKeyFromJwks(jwks, kid))));
        assertEquals(parsed.getJWTClaimsSet().getSubject(), machineClientId);
        assertEquals(parsed.getJWTClaimsSet().getAudience().get(0), "filebrowser");
        assertEquals(parsed.getJWTClaimsSet().getStringClaim("azp"), machineClientId);
        assertEquals(parsed.getJWTClaimsSet().getStringClaim("scope"), "openid profile");

    }

    @Test(dependsOnMethods = "clientCredentials_issuesMachineTokenVerifiableAgainstJwks")
    public void clientCredentials_rejectsUnregisteredAudience() throws Exception {

        final var form = new Form();
        form.param("grant_type", "client_credentials");
        form.param("client_id", machineClientId);
        form.param("client_secret", machineClientSecret);
        form.param("audience", "not-registered");

        final Response response = client
                .target(apiRoot + "/oidc/token")
                .request(APPLICATION_JSON)
                .post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

        assertEquals(response.getStatus(), 400);
        assertEquals(objectMapper.readTree(response.readEntity(String.class)).get("error").asText(),
                "invalid_target");

    }

    @Test(dependsOnMethods = "createClientRegistration_returnsSecretOnce")
    public void tokenEndpoint_rateLimited_returns429() {

        final var created = registerMachineClient();
        rateLimitClientId = created.getClientId();
        rateLimitClientSecret = created.getClientSecret();

        // The test deployment budgets 10 token requests per client per minute; the probe client spends
        // them all and expects the next request to be refused with 429.

        var saw429 = false;

        for (int i = 0; i < 12; i++) {

            final var form = new Form();
            form.param("grant_type", "client_credentials");
            form.param("client_id", rateLimitClientId);
            form.param("client_secret", rateLimitClientSecret);

            final Response response = client
                    .target(apiRoot + "/oidc/token")
                    .request(APPLICATION_JSON)
                    .post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

            if (response.getStatus() == 429) {
                saw429 = true;
                break;
            }

            assertEquals(response.getStatus(), 200, "Request " + i + " unexpectedly failed.");

        }

        assertTrue(saw429, "The token endpoint never returned 429 within the budget.");

    }

    /**
     * Runs a fresh authorization to completion: authorize, hosted login, and the consent decision.
     *
     * @param scope the scope to request
     * @param decision "approve" or "deny"
     * @return the value of the {@code Location} header the consent decision produced
     */
    private String completeNewAuthorization(final String scope, final String decision) {

        final var authorize = client
                .target(apiRoot + "/oidc/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("scope", scope)
                .queryParam("state", STATE)
                .queryParam("nonce", NONCE)
                .queryParam("code_challenge", PKCE_CHALLENGE)
                .queryParam("code_challenge_method", "S256")
                .request()
                .property(FOLLOW_REDIRECTS, false)
                .get();

        assertEquals(authorize.getStatus(), 303);
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

        assertEquals(login.getStatus(), 200, "Expected the consent screen.");

        final var consentForm = new Form();
        consentForm.param("authorizationId", hiddenAuthorizationId(login.readEntity(String.class)));
        consentForm.param("decision", decision);

        final var consent = client
                .target(apiRoot + "/oidc/consent")
                .request()
                .property(FOLLOW_REDIRECTS, false)
                .post(Entity.entity(consentForm, APPLICATION_FORM_URLENCODED));

        assertEquals(consent.getStatus(), 303);

        return consent.getLocation().toString();

    }

    /**
     * Registers a machine-only client (client_credentials, audience-scoped, no redirect URIs).
     */
    private OidcClientRegistration registerMachineClient() {

        final var registration = new OidcClientRegistration();
        registration.setDisplayName("Machine integration client");
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.CLIENT_CREDENTIALS));
        registration.setAudiences(Set.of("filebrowser"));
        registration.setScopes(Set.of("openid", "profile"));

        final Response response = client
                .target(apiRoot + "/oidc_client")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(registration, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        return response.readEntity(OidcClientRegistration.class);

    }

    private String hiddenAuthorizationId(final String html) {

        final var marker = "name=\"authorizationId\" value=\"";
        final var start = html.indexOf(marker);

        assertTrue(start >= 0, "No hidden authorizationId in: " + html);

        final var from = start + marker.length();
        final var end = html.indexOf('"', from);

        return html.substring(from, end);

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
