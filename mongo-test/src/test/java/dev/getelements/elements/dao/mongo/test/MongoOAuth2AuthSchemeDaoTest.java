package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.OAuth2AuthSchemeDao;
import dev.getelements.elements.sdk.model.auth.HttpMethod;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.exception.auth.AuthSchemeNotFoundException;
import org.bson.types.ObjectId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoOAuth2AuthSchemeDaoTest {

    private static final String TEST_NAME = "test_oauth2_scheme";

    private OAuth2AuthSchemeDao oAuth2AuthSchemeDao;

    private final Map<String, OAuth2AuthScheme> intermediateAuthSchemes = new ConcurrentHashMap<>();

    private void updateIntermediate(final OAuth2AuthScheme authScheme) {
        intermediateAuthSchemes.put(authScheme.getId(), authScheme);
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

        final var toCreate = new OAuth2AuthScheme();
        toCreate.setName(format("%s_%d", TEST_NAME, iteration));
        toCreate.setValidationUrl(format("https://example.com/validate/%d", iteration));
        toCreate.setMethod(HttpMethod.GET);

        final var created = getOAuth2AuthSchemeDao().createAuthScheme(toCreate);
        assertNotNull(created.getId());
        assertEquals(toCreate.getName(), created.getName());
        assertEquals(toCreate.getValidationUrl(), created.getValidationUrl());
        assertEquals(toCreate.getMethod(), created.getMethod());

        updateIntermediate(created);

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
    public void testFindIndividualAuthScheme(final String authSchemeId, final OAuth2AuthScheme existing) {
        final var fetched = getOAuth2AuthSchemeDao().findAuthScheme(authSchemeId);
        assertEquals(authSchemeId, fetched.get().getId());
    }

    @Test(dependsOnMethods = "testCreateAuthScheme")
    public void testFindIndividualAuthSchemeFail() {
        final var fetched = getOAuth2AuthSchemeDao().findAuthScheme(new ObjectId().toHexString());
        assertTrue(fetched.isEmpty());
    }

    @Test(dependsOnMethods = "testFindIndividualAuthScheme")
    public void testGetAllAuthSchemes() {

        final var fetched = getOAuth2AuthSchemeDao().getAuthSchemes(0, 100, null);
        assertEquals(intermediateAuthSchemes.size(), fetched.getTotal());

        for (var scheme : fetched.getObjects()) {
            assertTrue(intermediateAuthSchemes.containsKey(scheme.getId()));
        }

    }

    @Test(dependsOnMethods = "testGetAllAuthSchemes", dataProvider = "getIntermediates")
    public void updateAuthScheme(final String authSchemeId, final OAuth2AuthScheme existing) {

        final var toUpdate = new OAuth2AuthScheme();
        toUpdate.setId(authSchemeId);
        toUpdate.setName(format("%s_updated", existing.getName()));
        toUpdate.setValidationUrl(format("%s_updated", existing.getValidationUrl()));
        toUpdate.setMethod(HttpMethod.POST);

        final var updated = getOAuth2AuthSchemeDao().updateAuthScheme(toUpdate);

        assertEquals(toUpdate.getId(), updated.getId());
        assertEquals(toUpdate.getName(), updated.getName());
        assertEquals(toUpdate.getValidationUrl(), updated.getValidationUrl());
        assertEquals(toUpdate.getMethod(), updated.getMethod());

        updateIntermediate(updated);

    }

    @Test(dependsOnMethods = "updateAuthScheme", dataProvider = "getIntermediates")
    public void testDeleteAuthScheme(final String authSchemeId, final OAuth2AuthScheme authScheme) {
        getOAuth2AuthSchemeDao().deleteAuthScheme(authSchemeId);
        assertTrue(getOAuth2AuthSchemeDao().findAuthScheme(authSchemeId).isEmpty());
    }

    @Test(dependsOnMethods = "testDeleteAuthScheme", dataProvider = "getIntermediates",
          expectedExceptions = AuthSchemeNotFoundException.class)
    public void testDeleteAuthSchemeTwiceFails(final String authSchemeId, final OAuth2AuthScheme authScheme) {
        getOAuth2AuthSchemeDao().deleteAuthScheme(authSchemeId);
    }

    @Test(dependsOnMethods = "testDeleteAuthSchemeTwiceFails")
    public void testAllAreDeleted() {
        final var fetched = getOAuth2AuthSchemeDao().getAuthSchemes(0, 100, null);
        assertEquals(fetched.getTotal(), 0);
        assertEquals(fetched.getObjects().size(), 0);
    }

    public OAuth2AuthSchemeDao getOAuth2AuthSchemeDao() {
        return oAuth2AuthSchemeDao;
    }

    @Inject
    public void setOAuth2AuthSchemeDao(OAuth2AuthSchemeDao oAuth2AuthSchemeDao) {
        this.oAuth2AuthSchemeDao = oAuth2AuthSchemeDao;
    }

}
