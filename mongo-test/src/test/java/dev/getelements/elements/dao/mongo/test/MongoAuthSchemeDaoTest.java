package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.AuthSchemeDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.auth.AuthSchemeNotFoundException;
import dev.getelements.elements.sdk.model.auth.AuthScheme;
import dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.getelements.elements.sdk.model.user.User;
import org.bson.types.ObjectId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoAuthSchemeDaoTest {

    private static final String TEST_AUDIENCE = "test";

    private static final List<String> TEST_TAGS_UPDATE = List.of("tag0", "tag1");

    private static final List<String> TEST_ISSUERS = List.of("issuer0", "issuer1");

    private static final List<String> TEST_ISSUERS_UPDATE = List.of("issuer2", "issuer3");

    private AuthSchemeDao authSchemeDao;

    private final Map<String, AuthScheme> intermediateAuthSchemes = new ConcurrentHashMap<>();

    private void updateIntermediate(final AuthScheme authScheme) {
        intermediateAuthSchemes.put(authScheme.getId(), authScheme);
    }

    private String generateMockKey() {
        final var bytes = new byte[512];
        ThreadLocalRandom.current().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @DataProvider
    public Object[][] getAuthSchemeIteration() {
        return IntStream
            .range(0, 10)
            .mapToObj(i -> new Object[]{i})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getAuthSchemeIteration")
    public void testCreateAuthScheme(final int iteration) {

        final var toCreate = new AuthScheme();
        toCreate.setAudience(format("%s_%d", TEST_AUDIENCE, iteration));
        toCreate.setPublicKey(generateMockKey());
        toCreate.setAlgorithm(PrivateKeyCrytpoAlgorithm.RSA_256);
        toCreate.setTags(emptyList());
        toCreate.setAllowedIssuers(TEST_ISSUERS);
        toCreate.setUserLevel(User.Level.USER);

        final var created = getAuthSchemeDao().createAuthScheme(toCreate);
        assertNotNull(created.getId());
        assertEquals(toCreate.getTags(), created.getTags());
        assertEquals(toCreate.getAudience(), created.getAudience());
        assertEquals(toCreate.getPublicKey(), created.getPublicKey());
        assertEquals(toCreate.getAlgorithm(), created.getAlgorithm());
        assertEquals(toCreate.getAllowedIssuers(), created.getAllowedIssuers());
        assertEquals(toCreate.getUserLevel(), created.getUserLevel());

        updateIntermediate(created);

    }

    @Test(dataProvider = "getAuthSchemeIteration",
          dependsOnMethods = "testCreateAuthScheme",
          expectedExceptions = DuplicateException.class)
    public void testCreateAuthSchemeDuplicateAudience(final int iteration) {
        final var toCreate = new AuthScheme();
        toCreate.setTags(emptyList());
        toCreate.setAudience(format("%s_%d", TEST_AUDIENCE, iteration));
        toCreate.setPublicKey(generateMockKey());
        toCreate.setAlgorithm(PrivateKeyCrytpoAlgorithm.RSA_256);
        toCreate.setAllowedIssuers(List.of("issuer0", "issuer1"));
        toCreate.setUserLevel(User.Level.USER);
        getAuthSchemeDao().createAuthScheme(toCreate);
    }

    @DataProvider
    public Object[][] getIntermediates() {
        return intermediateAuthSchemes
            .entrySet()
            .stream()
            .map(e -> new Object[]{e.getKey(), e.getValue()})
            .toArray(Object[][]::new);
    }

    @Test(dependsOnMethods = "testCreateAuthScheme", dataProvider = "getIntermediates")
    public void testGetIndividualAuthScheme(final String authSchemeId, final AuthScheme existing) {
        final var fetched = getAuthSchemeDao().getAuthScheme(authSchemeId);
        assertEquals(authSchemeId, fetched.getId());
    }

    @Test(dependsOnMethods = "testCreateAuthScheme", dataProvider = "getIntermediates")
    public void testFindIndividualAuthScheme(final String authSchemeId, final AuthScheme existing) {
        final var fetched = getAuthSchemeDao().findAuthScheme(authSchemeId);
        assertEquals(authSchemeId, fetched.get().getId());
    }

    @Test(dependsOnMethods = "testCreateAuthScheme", expectedExceptions = AuthSchemeNotFoundException.class)
    public void testGetIndividualAuthSchemeFail() {
        getAuthSchemeDao().getAuthScheme(new ObjectId().toHexString());
    }

    @Test(dependsOnMethods = "testCreateAuthScheme")
    public void testFindIndividualAuthSchemeFail() {
        final var fetched = getAuthSchemeDao().findAuthScheme(new ObjectId().toHexString());
        assertTrue(fetched.isEmpty());
    }

    @Test(dependsOnMethods = {"testGetIndividualAuthScheme", "testFindIndividualAuthScheme"})
    public void testGetAllAuthSchemes() {

        final var fetched = getAuthSchemeDao().getAuthSchemes(0, 100, null);
        assertEquals(intermediateAuthSchemes.size(), fetched.getTotal());

        for (var scheme : fetched.getObjects()) {
            assertTrue(intermediateAuthSchemes.containsKey(scheme.getId()));
        }

    }

    @Test(dependsOnMethods = {"testGetIndividualAuthScheme", "testFindIndividualAuthScheme"})
    public void testGetAllByAudience() {

        final var audiences = IntStream
            .range(0, 10)
            .mapToObj(i -> format("%s_%d", TEST_AUDIENCE, i))
            .collect(Collectors.toList());

        final var schemes = getAuthSchemeDao().getAuthSchemesByAudience(audiences);
        assertEquals(schemes.size(), intermediateAuthSchemes.size());

        for (var scheme : schemes) {
            assertTrue(audiences.contains(scheme.getAudience()));
            assertTrue(intermediateAuthSchemes.containsKey(scheme.getId()));
        }

    }

    @Test(dataProvider = "getAuthSchemeIteration",
          dependsOnMethods = {"testGetIndividualAuthScheme", "testFindIndividualAuthScheme"}
    )
    public void testGetByAudience(final int iteration) {

        final var audiences = List.of(format("%s_%d", TEST_AUDIENCE, iteration));
        final var schemes = getAuthSchemeDao().getAuthSchemesByAudience(audiences);

        assertEquals(schemes.size(), 1);

        final var authScheme = schemes.get(0);
        final var authSchemeId = authScheme.getId();
        assertTrue(audiences.contains(authScheme.getAudience()));
        assertTrue(intermediateAuthSchemes.containsKey(authSchemeId));

    }

    @Test(dependsOnMethods = {"testGetAllAuthSchemes", "testGetByAudience", "testGetAllByAudience"},
          dataProvider = "getIntermediates")
    public void updateAuthScheme(final String authSchemeId, final AuthScheme existing) {

        final var toUpdate = new AuthScheme();
        toUpdate.setId(authSchemeId);
        toUpdate.setAudience(format("%s_updated", existing.getAudience()));
        toUpdate.setPublicKey(generateMockKey());
        toUpdate.setAlgorithm(PrivateKeyCrytpoAlgorithm.RSA_384);
        toUpdate.setTags(TEST_TAGS_UPDATE);
        toUpdate.setAllowedIssuers(TEST_ISSUERS_UPDATE);
        toUpdate.setUserLevel(User.Level.SUPERUSER);

        final var updated = getAuthSchemeDao().updateAuthScheme(toUpdate);

        assertEquals(toUpdate.getId(), updated.getId());
        assertEquals(toUpdate.getTags(), updated.getTags());
        assertEquals(toUpdate.getAudience(), updated.getAudience());
        assertEquals(toUpdate.getPublicKey(), updated.getPublicKey());
        assertEquals(toUpdate.getAlgorithm(), updated.getAlgorithm());
        assertEquals(toUpdate.getAllowedIssuers(), updated.getAllowedIssuers());
        assertEquals(toUpdate.getUserLevel(), updated.getUserLevel());

        updateIntermediate(updated);

    }

    @Test(dependsOnMethods = "updateAuthScheme",
          dataProvider = "getIntermediates",
          expectedExceptions = DuplicateException.class)
    public void updateAuthSchemeDuplicate(final String authSchemeId, final AuthScheme existing) {

        // Create a valid auth scheme that has an alternative audience and try to update it to the main test scheme

        final var toCreate = new AuthScheme();
        toCreate.setTags(emptyList());
        toCreate.setAudience(format("%s_alternative", TEST_AUDIENCE));
        toCreate.setPublicKey(generateMockKey());
        toCreate.setAlgorithm(PrivateKeyCrytpoAlgorithm.RSA_256);
        toCreate.setAllowedIssuers(TEST_ISSUERS);
        toCreate.setUserLevel(User.Level.USER);

        final var created = getAuthSchemeDao().createAuthScheme(toCreate);

        try {
            created.setAudience(existing.getAudience());
            getAuthSchemeDao().updateAuthScheme(created);
        } finally {
            getAuthSchemeDao().deleteAuthScheme(created.getId());
        }

    }

    @Test(dependsOnMethods = "updateAuthSchemeDuplicate", dataProvider = "getIntermediates")
    public void testDeleteAuthScheme(final String authSchemeId, final AuthScheme authScheme) {
        getAuthSchemeDao().deleteAuthScheme(authSchemeId);
        assertTrue(getAuthSchemeDao().findAuthScheme(authSchemeId).isEmpty());
        intermediateAuthSchemes.remove(authSchemeId);
    }

    @Test(dependsOnMethods = "testDeleteAuthScheme")
    public void testAllAreDeleted() {
        final var fetched = getAuthSchemeDao().getAuthSchemes(0, 100, null);
        assertEquals(fetched.getTotal(), 0);
        assertEquals(fetched.getObjects().size(), 0);
    }

    public AuthSchemeDao getAuthSchemeDao() {
        return authSchemeDao;
    }

    @Inject
    public void setAuthSchemeDao(AuthSchemeDao authSchemeDao) {
        this.authSchemeDao = authSchemeDao;
    }

}
