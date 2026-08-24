package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.OidcAuthSchemeDao;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.exception.auth.AuthSchemeNotFoundException;
import org.bson.types.ObjectId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoOidcAuthSchemeDaoTest {

    private static final String TEST_ISSUER = "test_oidc_issuer";

    private OidcAuthSchemeDao oidcAuthSchemeDao;

    private final Map<String, OidcAuthScheme> intermediateAuthSchemes = new ConcurrentHashMap<>();

    private void updateIntermediate(final OidcAuthScheme authScheme) {
        intermediateAuthSchemes.put(authScheme.getId(), authScheme);
    }

    private JWK testKey(final int iteration) {
        final var jwk = new JWK();
        jwk.setAlg("RS256");
        jwk.setKid(format("kid_%d", iteration));
        jwk.setKty("RSA");
        jwk.setUse("sig");
        jwk.setE("AQAB");
        jwk.setN(format("modulus_%d", iteration));
        return jwk;
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

        final var toCreate = new OidcAuthScheme();
        toCreate.setName(format("%s_%d", TEST_ISSUER, iteration));
        toCreate.setIssuer(format("https://example.com/issuer/%d", iteration));
        toCreate.setKeys(List.of(testKey(iteration)));

        final var created = getOidcAuthSchemeDao().createAuthScheme(toCreate);
        assertNotNull(created.getId());
        assertEquals(toCreate.getName(), created.getName());
        assertEquals(toCreate.getIssuer(), created.getIssuer());
        assertEquals(toCreate.getKeys(), created.getKeys());

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
    public void testFindIndividualAuthScheme(final String authSchemeId, final OidcAuthScheme existing) {
        final var fetched = getOidcAuthSchemeDao().findAuthScheme(authSchemeId);
        assertEquals(authSchemeId, fetched.get().getId());
    }

    @Test(dependsOnMethods = "testCreateAuthScheme")
    public void testFindIndividualAuthSchemeFail() {
        final var fetched = getOidcAuthSchemeDao().findAuthScheme(new ObjectId().toHexString());
        assertTrue(fetched.isEmpty());
    }

    @Test(dependsOnMethods = "testFindIndividualAuthScheme")
    public void testGetAllAuthSchemes() {

        final var fetched = getOidcAuthSchemeDao().getAuthSchemes(0, 100, null);
        assertEquals(intermediateAuthSchemes.size(), fetched.getTotal());

        for (var scheme : fetched.getObjects()) {
            assertTrue(intermediateAuthSchemes.containsKey(scheme.getId()));
        }

    }

    @Test(dependsOnMethods = "testGetAllAuthSchemes", dataProvider = "getIntermediates")
    public void updateAuthScheme(final String authSchemeId, final OidcAuthScheme existing) {

        final var toUpdate = new OidcAuthScheme();
        toUpdate.setId(authSchemeId);
        toUpdate.setName(format("%s_updated", existing.getName()));
        toUpdate.setIssuer(format("%s_updated", existing.getIssuer()));
        toUpdate.setKeys(existing.getKeys());

        final var updated = getOidcAuthSchemeDao().updateAuthScheme(toUpdate);

        assertEquals(toUpdate.getId(), updated.getId());
        assertEquals(toUpdate.getName(), updated.getName());
        assertEquals(toUpdate.getIssuer(), updated.getIssuer());

        updateIntermediate(updated);

    }

    @Test(dependsOnMethods = "updateAuthScheme", dataProvider = "getIntermediates")
    public void testDeleteAuthScheme(final String authSchemeId, final OidcAuthScheme authScheme) {
        getOidcAuthSchemeDao().deleteAuthScheme(authSchemeId);
        assertTrue(getOidcAuthSchemeDao().findAuthScheme(authSchemeId).isEmpty());
    }

    @Test(dependsOnMethods = "testDeleteAuthScheme", dataProvider = "getIntermediates",
          expectedExceptions = AuthSchemeNotFoundException.class)
    public void testDeleteAuthSchemeTwiceFails(final String authSchemeId, final OidcAuthScheme authScheme) {
        getOidcAuthSchemeDao().deleteAuthScheme(authSchemeId);
    }

    @Test(dependsOnMethods = "testDeleteAuthSchemeTwiceFails")
    public void testAllAreDeleted() {
        final var fetched = getOidcAuthSchemeDao().getAuthSchemes(0, 100, null);
        assertEquals(fetched.getTotal(), 0);
        assertEquals(fetched.getObjects().size(), 0);
    }

    public OidcAuthSchemeDao getOidcAuthSchemeDao() {
        return oidcAuthSchemeDao;
    }

    @Inject
    public void setOidcAuthSchemeDao(OidcAuthSchemeDao oidcAuthSchemeDao) {
        this.oidcAuthSchemeDao = oidcAuthSchemeDao;
    }

}
