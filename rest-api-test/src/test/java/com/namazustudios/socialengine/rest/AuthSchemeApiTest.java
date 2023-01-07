package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;
import com.namazustudios.socialengine.model.ErrorResponse;
import com.namazustudios.socialengine.model.auth.*;
import com.namazustudios.socialengine.rest.model.AuthSchemePagination;
import com.namazustudios.socialengine.service.util.CryptoKeyPairUtility;
import com.namazustudios.socialengine.service.util.StandardCryptoKeyPairUtility;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.exception.ErrorCode.FORBIDDEN;
import static com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm.ECDSA_256;
import static com.namazustudios.socialengine.model.user.User.Level.SUPERUSER;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class AuthSchemeApiTest {

    private static final String TAG_SUPPLIED = "supplied";

    private static final String TAG_GENERATED = "generated";

    private static final List<String> TEST_ALLOWED_ISSUERS = List.of("issuer0", "issuer1");

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(AuthSchemeApiTest.class),
                TestUtils.getInstance().getUnixFSTest(AuthSchemeApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user;

    @Inject
    private ClientContext superUser;

    private CryptoKeyPairUtility cryptoKeyPairUtility;

    @BeforeClass
    public void setupSuperuser() {

        user.createUser("luser")
            .createSession();

        superUser.createSuperuser("root")
                 .createSession();

    }

    private final Map<String, AuthScheme> intermediateAuthSchemes = new ConcurrentHashMap<>();

    private void updateIntermediate(final AuthScheme authScheme) {
        intermediateAuthSchemes.put(authScheme.getId(), authScheme);
    }

    @BeforeClass
    public void setup() throws Exception {
        cryptoKeyPairUtility = new StandardCryptoKeyPairUtility();
    }

    @DataProvider
    private Object[][] getAlgorithms() {
        return Stream.of(PrivateKeyCrytpoAlgorithm.values())
            .map(algo -> new Object[]{algo})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getAlgorithms")
    public void createAuthSchemeGeneratingPublicKey(final PrivateKeyCrytpoAlgorithm algorithm) {

        final var createRequest = new CreateAuthSchemeRequest();

        createRequest.setAlgorithm(algorithm);
        createRequest.setUserLevel(SUPERUSER);
        createRequest.setAudience(format("aud_gen_%d", algorithm.ordinal()));
        createRequest.setAllowedIssuers(TEST_ALLOWED_ISSUERS);
        createRequest.setTags(List.of(algorithm.toString(), TAG_GENERATED));

        final var response = client
            .target(apiRoot + "/auth_scheme")
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .post(Entity.entity(createRequest, APPLICATION_JSON))
            .readEntity(CreateAuthSchemeResponse.class);

        assertNotNull(response);
        assertNotNull(response.getPublicKey());
        assertNotNull(response.getPrivateKey());
        cryptoKeyPairUtility.getPublicKey(algorithm, response.getPublicKey());
        cryptoKeyPairUtility.getPrivateKey(algorithm, response.getPrivateKey());

        final var scheme = response.getScheme();
        assertEquals(scheme.getAlgorithm(), algorithm);
        assertEquals(scheme.getPublicKey(), response.getPublicKey());
        assertEquals(scheme.getTags(), createRequest.getTags());
        assertEquals(scheme.getAllowedIssuers(), createRequest.getAllowedIssuers());

        updateIntermediate(scheme);

    }

    @Test(dataProvider = "getAlgorithms")
    public void createAuthSchemeSupplyingPublicKey(final PrivateKeyCrytpoAlgorithm algorithm) {

        final var createRequest = new CreateAuthSchemeRequest();
        final var keyPair = cryptoKeyPairUtility.generateKeyPair(algorithm);

        createRequest.setAlgorithm(algorithm);
        createRequest.setUserLevel(SUPERUSER);
        createRequest.setAudience(format("aud_supplied_%d", algorithm.ordinal()));
        createRequest.setAllowedIssuers(TEST_ALLOWED_ISSUERS);
        createRequest.setTags(List.of(algorithm.toString(), TAG_SUPPLIED));
        createRequest.setPublicKey(keyPair.getPublicKeyBase64());

        final var response = client
            .target(apiRoot + "/auth_scheme")
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .post(Entity.entity(createRequest, APPLICATION_JSON))
            .readEntity(CreateAuthSchemeResponse.class);

        assertNotNull(response);
        assertNull(response.getPrivateKey());
        assertNotNull(response.getPublicKey());
        cryptoKeyPairUtility.getPublicKey(algorithm, response.getPublicKey());

        final var scheme = response.getScheme();
        assertEquals(scheme.getAlgorithm(), algorithm);
        assertEquals(scheme.getPublicKey(), response.getPublicKey());
        assertEquals(scheme.getTags(), createRequest.getTags());
        assertEquals(scheme.getAllowedIssuers(), createRequest.getAllowedIssuers());

        updateIntermediate(scheme);

    }

    @Test
    public void testUserIsForbiddenCreate() {

        final var createRequest = new CreateAuthSchemeRequest();
        final var keyPair = cryptoKeyPairUtility.generateKeyPair(ECDSA_256);

        createRequest.setAlgorithm(ECDSA_256);
        createRequest.setUserLevel(SUPERUSER);
        createRequest.setAudience(format("aud_supplied_%d", ECDSA_256.ordinal()));
        createRequest.setAllowedIssuers(TEST_ALLOWED_ISSUERS);
        createRequest.setTags(List.of(ECDSA_256.toString(), TAG_GENERATED));
        createRequest.setPublicKey(keyPair.getPublicKeyBase64());

        final var response = client
            .target(apiRoot + "/auth_scheme")
            .request()
            .header("Authorization", format("Bearer %s", user.getSessionSecret()))
            .post(Entity.entity(createRequest, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(error.getCode(), FORBIDDEN.toString());

    }

    @Test
    public void testAnonymousUserIsForbiddenCreate() {

        final var createRequest = new CreateAuthSchemeRequest();
        final var keyPair = cryptoKeyPairUtility.generateKeyPair(ECDSA_256);

        createRequest.setAlgorithm(ECDSA_256);
        createRequest.setUserLevel(SUPERUSER);
        createRequest.setAudience(format("aud_supplied_%d", ECDSA_256.ordinal()));
        createRequest.setAllowedIssuers(TEST_ALLOWED_ISSUERS);
        createRequest.setTags(List.of(ECDSA_256.toString(), TAG_GENERATED));
        createRequest.setPublicKey(keyPair.getPublicKeyBase64());

        final var response = client
            .target(apiRoot + "/auth_scheme")
            .request()
            .post(Entity.entity(createRequest, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(error.getCode(), FORBIDDEN.toString());

    }

    @DataProvider
    public Object[][] getIntermediatesGenerated() {
        return intermediateAuthSchemes
            .entrySet()
            .stream()
            .filter(e -> e.getValue().getTags().contains(TAG_GENERATED))
            .map(e -> new Object[]{e.getKey(), e.getValue()})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getIntermediatesGenerated", dependsOnMethods = "createAuthSchemeGeneratingPublicKey")
    public void updateAuthSchemeGeneratingPublicKey(final String authSchemeId, final AuthScheme authScheme) {

        final var updateRequest = new UpdateAuthSchemeRequest();

        updateRequest.setRegenerate(true);
        updateRequest.setUserLevel(SUPERUSER);
        updateRequest.setTags(authScheme.getTags());
        updateRequest.setAudience(authScheme.getAudience());
        updateRequest.setAlgorithm(authScheme.getAlgorithm());
        updateRequest.setAllowedIssuers(TEST_ALLOWED_ISSUERS);

        final var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .put(Entity.entity(updateRequest, APPLICATION_JSON))
            .readEntity(UpdateAuthSchemeResponse.class);

        assertNotNull(response);
        assertNotNull(response.getPublicKey());
        assertNotNull(response.getPrivateKey());
        assertNotEquals(response.getPublicKey(), authScheme.getPublicKey());

        cryptoKeyPairUtility.getPublicKey(authScheme.getAlgorithm(), response.getPublicKey());
        cryptoKeyPairUtility.getPrivateKey(authScheme.getAlgorithm(), response.getPrivateKey());

        final var scheme = response.getScheme();
        assertEquals(scheme.getAlgorithm(), updateRequest.getAlgorithm());
        assertEquals(scheme.getPublicKey(), response.getPublicKey());
        assertEquals(scheme.getTags(), updateRequest.getTags());
        assertEquals(scheme.getAllowedIssuers(), updateRequest.getAllowedIssuers());

        updateIntermediate(scheme);

    }

    @DataProvider
    public Object[][] getIntermediatesSupplied() {
        return intermediateAuthSchemes
            .entrySet()
            .stream()
            .filter(e -> e.getValue().getTags().contains(TAG_SUPPLIED))
            .map(e -> new Object[]{e.getKey(), e.getValue()})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getIntermediatesSupplied", dependsOnMethods = "createAuthSchemeSupplyingPublicKey")
    public void updateAuthSchemeSupplyingPublicKey(final String authSchemeId, final AuthScheme authScheme) {

        final var keyPair = cryptoKeyPairUtility.generateKeyPair(authScheme.getAlgorithm());

        final var updateRequest = new UpdateAuthSchemeRequest();
        updateRequest.setRegenerate(false);
        updateRequest.setUserLevel(SUPERUSER);
        updateRequest.setTags(authScheme.getTags());
        updateRequest.setAudience(authScheme.getAudience());
        updateRequest.setAlgorithm(authScheme.getAlgorithm());
        updateRequest.setAllowedIssuers(TEST_ALLOWED_ISSUERS);
        updateRequest.setPublicKey(keyPair.getPublicKeyBase64());

        final var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .put(Entity.entity(updateRequest, APPLICATION_JSON))
            .readEntity(UpdateAuthSchemeResponse.class);

        assertNotNull(response);
        assertNull(response.getPrivateKey());
        assertNotNull(response.getPublicKey());
        cryptoKeyPairUtility.getPublicKey(authScheme.getAlgorithm(), response.getPublicKey());

        final var scheme = response.getScheme();
        assertEquals(scheme.getAlgorithm(), updateRequest.getAlgorithm());
        assertEquals(scheme.getPublicKey(), response.getPublicKey());
        assertEquals(scheme.getTags(), updateRequest.getTags());
        assertEquals(scheme.getAllowedIssuers(), updateRequest.getAllowedIssuers());

        updateIntermediate(scheme);

    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"createAuthSchemeGeneratingPublicKey", "createAuthSchemeSupplyingPublicKey"})
    public void testUpdateWithNoKeyChange(final String authSchemeId, final AuthScheme authScheme) {

        final var updateRequest = new UpdateAuthSchemeRequest();
        updateRequest.setRegenerate(false);
        updateRequest.setUserLevel(SUPERUSER);
        updateRequest.setTags(authScheme.getTags());
        updateRequest.setAudience(authScheme.getAudience());
        updateRequest.setAlgorithm(authScheme.getAlgorithm());
        updateRequest.setAllowedIssuers(TEST_ALLOWED_ISSUERS);

        final var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .put(Entity.entity(updateRequest, APPLICATION_JSON))
            .readEntity(UpdateAuthSchemeResponse.class);

        assertNotNull(response);
        assertNull(response.getPrivateKey());
        assertNotNull(response.getPublicKey());
        cryptoKeyPairUtility.getPublicKey(authScheme.getAlgorithm(), response.getPublicKey());

        final var scheme = response.getScheme();
        assertEquals(scheme.getAlgorithm(), updateRequest.getAlgorithm());
        assertEquals(scheme.getPublicKey(), response.getPublicKey());
        assertEquals(scheme.getTags(), updateRequest.getTags());
        assertEquals(scheme.getAllowedIssuers(), updateRequest.getAllowedIssuers());

        updateIntermediate(scheme);

    }

    @DataProvider
    public Object[][] getIntermediates() {
        return intermediateAuthSchemes
                .entrySet()
                .stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"createAuthSchemeGeneratingPublicKey", "createAuthSchemeSupplyingPublicKey"})
    public void testUserIsForbiddenUpdate(final String authSchemeId, final AuthScheme authScheme) {

        final var updateRequest = new UpdateAuthSchemeRequest();

        updateRequest.setRegenerate(true);
        updateRequest.setUserLevel(SUPERUSER);
        updateRequest.setTags(authScheme.getTags());
        updateRequest.setAudience(authScheme.getAudience());
        updateRequest.setAlgorithm(authScheme.getAlgorithm());
        updateRequest.setAllowedIssuers(TEST_ALLOWED_ISSUERS);

        final var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .header("Authorization", format("Bearer %s", user.getSessionSecret()))
            .put(Entity.entity(updateRequest, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(error.getCode(), FORBIDDEN.toString());

    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"createAuthSchemeGeneratingPublicKey", "createAuthSchemeSupplyingPublicKey"})
    public void testAnonymousUserIsForbiddenUpdate(final String authSchemeId, final AuthScheme authScheme) {

        final var updateRequest = new UpdateAuthSchemeRequest();

        updateRequest.setRegenerate(true);
        updateRequest.setUserLevel(SUPERUSER);
        updateRequest.setTags(authScheme.getTags());
        updateRequest.setAudience(authScheme.getAudience());
        updateRequest.setAlgorithm(authScheme.getAlgorithm());
        updateRequest.setAllowedIssuers(TEST_ALLOWED_ISSUERS);

        final var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .put(Entity.entity(updateRequest, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(error.getCode(), FORBIDDEN.toString());

    }

    @Test(dependsOnMethods = {
        "testUpdateWithNoKeyChange",
        "updateAuthSchemeSupplyingPublicKey",
        "updateAuthSchemeGeneratingPublicKey"
    })
    public void getAllAuthSchemes() {

        final var response = client
            .target(apiRoot + "/auth_scheme?offset=0&count=100")
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(AuthSchemePagination.class);

        assertTrue(response.getTotal() > 0);

    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {
              "testUpdateWithNoKeyChange",
              "updateAuthSchemeSupplyingPublicKey",
              "updateAuthSchemeGeneratingPublicKey"
    })
    public void getSingleAuthScheme(final String authSchemeId, final AuthScheme authScheme) {

        final var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get()
            .readEntity(AuthScheme.class);

        assertEquals(response, authScheme);

    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"getAllAuthSchemes", "getSingleAuthScheme"})
    public void testUserIsForbiddenToDeleteAuthScheme(final String authSchemeId, final AuthScheme authScheme) {

        final var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .header("Authorization", format("Bearer %s", user.getSessionSecret()))
            .delete();

        assertEquals(response.getStatus(), 403);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(error.getCode(), FORBIDDEN.toString());

    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"getAllAuthSchemes", "getSingleAuthScheme"})
    public void testAnonymousUserIsForbiddenToDeleteAuthScheme(final String authSchemeId, final AuthScheme authScheme) {
        final var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .delete();

        assertEquals(response.getStatus(), 403);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(error.getCode(), FORBIDDEN.toString());

    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"getAllAuthSchemes", "getSingleAuthScheme"})
    public void deleteAuthScheme(final String authSchemeId, final AuthScheme authScheme) {

        var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .delete();

        assertEquals(response.getStatus(), 204);

    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"deleteAuthScheme"})
    public void doubleDeleteAuthSchemeReturns404(final String authSchemeId, final AuthScheme authScheme) {

        var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .delete();

        assertEquals(response.getStatus(), 404);

    }

    @Test(dataProvider = "getIntermediates",
          dependsOnMethods = {"deleteAuthScheme"})
    public void getPostDeleteReturns404(final String authSchemeId, final AuthScheme authScheme) {

        var response = client
            .target(format("%s/auth_scheme/%s", apiRoot, authSchemeId))
            .request()
            .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
            .get();

        assertEquals(response.getStatus(), 404);

    }

}
