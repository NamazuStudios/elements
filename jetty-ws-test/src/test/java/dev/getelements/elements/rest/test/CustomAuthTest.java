package dev.getelements.elements.rest.test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.dao.CustomAuthUserDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.ErrorCode;
import dev.getelements.elements.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.getelements.elements.model.ErrorResponse;
import dev.getelements.elements.model.auth.*;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rest.test.ClientContext;
import dev.getelements.elements.rest.test.TestUtils;
import dev.getelements.elements.security.CustomJWTCredentials;
import dev.getelements.elements.service.util.CryptoKeyPairUtility;
import dev.getelements.elements.service.util.StandardCryptoKeyPairUtility;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import static dev.getelements.elements.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CustomAuthTest {

    private static final String TEST_ISSUER = "RestApiTestIssuer";

    private static final String TEST_AUDIENCE = "Audience";

    private static final String TAG_GENERATED = "generated";

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(CustomAuthTest.class),
            TestUtils.getInstance().getUnixFSTest(CustomAuthTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUser;

    @Inject
    private UserDao userDao;

    @Inject
    private CustomAuthUserDao customAuthUserDao;

    private CryptoKeyPairUtility cryptoKeyPairUtility;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Collection<SchemeTuple> schemesAndAlgorithms = new ConcurrentLinkedDeque<>();

    @BeforeClass
    public void setupAuthSchemes() throws NoSuchAlgorithmException {

        cryptoKeyPairUtility = new StandardCryptoKeyPairUtility();

        superUser
            .createSuperuser("root")
            .createSession();

        for (var level : User.Level.values()) {
            for (var authSchemeAlgorithm : PrivateKeyCrytpoAlgorithm.values()) {

                final var createRequest = new CreateAuthSchemeRequest();

                createRequest.setUserLevel(level);
                createRequest.setAlgorithm(authSchemeAlgorithm);
                createRequest.setAllowedIssuers(List.of(TEST_ISSUER));
                createRequest.setAudience(format("%s_%s_%d", TEST_AUDIENCE, level, authSchemeAlgorithm.ordinal()));
                createRequest.setTags(List.of(authSchemeAlgorithm.toString(), TAG_GENERATED));

                final var response = client
                        .target(apiRoot + "/auth_scheme")
                        .request()
                        .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                        .post(Entity.entity(createRequest, APPLICATION_JSON));

                assertEquals(response.getStatus(), 200);

                final var schemeCreation = response.readEntity(CreateAuthSchemeResponse.class);
                final var signingAlgorithm = getSigningAlgorithm(schemeCreation);

                schemesAndAlgorithms.add(new SchemeTuple(schemeCreation.getScheme(), signingAlgorithm));

            }
        }

    }

    private Algorithm getSigningAlgorithm(final CreateAuthSchemeResponse schemeCreation) {
        switch (schemeCreation.getScheme().getAlgorithm()) {
            case RSA_256:
            case RSA_384:
            case RSA_512:
                return getRSASigningAlgorithm(schemeCreation);
            case ECDSA_256:
            case ECDSA_384:
            case ECDSA_512:
                return getECDSASigningAlgorithm(schemeCreation);
            default:
                throw new IllegalArgumentException("Unsupported algorithm.");
        }
    }

    private Algorithm getRSASigningAlgorithm(final CreateAuthSchemeResponse schemeCreation) {

        final var rsaPublicKey = cryptoKeyPairUtility.getPublicKey(
            schemeCreation.getScheme().getAlgorithm(),
            schemeCreation.getPublicKey(),
            RSAPublicKey.class
        );

        final var rsaPrivateKey = cryptoKeyPairUtility.getPrivateKey(
            schemeCreation.getScheme().getAlgorithm(),
            schemeCreation.getPrivateKey(),
            RSAPrivateKey.class
        );

        switch (schemeCreation.getScheme().getAlgorithm()) {
            case RSA_256:
                return Algorithm.RSA256(rsaPublicKey, rsaPrivateKey);
            case RSA_384:
                return Algorithm.RSA384(rsaPublicKey, rsaPrivateKey);
            case RSA_512:
                return Algorithm.RSA512(rsaPublicKey, rsaPrivateKey);
            default:
                throw new IllegalArgumentException("Unsupported RSA algorithm: " + schemeCreation.getScheme().getAlgorithm());
        }

    }

    private Algorithm getECDSASigningAlgorithm(final CreateAuthSchemeResponse schemeCreation) {

        final var ecdsaPublicKey = cryptoKeyPairUtility.getPublicKey(
            schemeCreation.getScheme().getAlgorithm(),
            schemeCreation.getPublicKey(),
            ECPublicKey.class
        );

        final var ecdsaPrivateKey = cryptoKeyPairUtility.getPrivateKey(
            schemeCreation.getScheme().getAlgorithm(),
            schemeCreation.getPrivateKey(),
            ECPrivateKey.class
        );

        switch (schemeCreation.getScheme().getAlgorithm()) {
            case ECDSA_256:
                return Algorithm.ECDSA256(ecdsaPublicKey, ecdsaPrivateKey);
            case ECDSA_384:
                return Algorithm.ECDSA384(ecdsaPublicKey, ecdsaPrivateKey);
            case ECDSA_512:
                return Algorithm.ECDSA512(ecdsaPublicKey, ecdsaPrivateKey);
            default:
                throw new IllegalArgumentException("Unsupported ECDSA algorithm: " + schemeCreation.getScheme().getAlgorithm());
        }

    }

    @DataProvider
    public Object[][] getSchemesAndAlgorithms() {
        return schemesAndAlgorithms
            .stream()
            .map(sp -> new Object[]{sp.scheme, sp.signingAlgorithm, sp.externalUserId})
            .toArray(Object[][]::new);
    }

    @Test(invocationCount = 5, dataProvider = "getSchemesAndAlgorithms")
    public void testCustomAuthHappy(final AuthScheme authScheme,
                                    final Algorithm algorithm,
                                    final String externalUserId) {

        final var userClaim = new UserClaim();
        userClaim.setExternalUserId(externalUserId);
        userClaim.setLevel(authScheme.getUserLevel());

        final var mapTypeRef = new TypeReference<Map<String, Object>>(){};
        final var userClaimRaw = objectMapper.convertValue(userClaim, mapTypeRef);
        userClaimRaw.entrySet().removeIf(e -> e.getValue() == null);

        final var expiry = currentTimeMillis() + MILLISECONDS.convert(1, HOURS);

        final var jwt = JWT.create()
           .withIssuer(TEST_ISSUER)
           .withAudience(authScheme.getAudience())
           .withExpiresAt(new Date(expiry))
           .withSubject(externalUserId)
           .withClaim(PrivateClaim.AUTH_TYPE.getValue(), CustomJWTCredentials.AUTH_TYPE)
           .withClaim(PrivateClaim.USER_KEY.getValue(), UserKey.EXTERNAL_USER_ID.getValue())
           .withClaim(PrivateClaim.USER.getValue(), userClaimRaw)
           .sign(algorithm);

        final var response = client
            .target(format("%s/user/me", apiRoot))
            .request()
            .header("Authorization", format("Bearer %s", jwt))
            .get();

        assertEquals(response.getStatus(), 200);

        final var user = response.readEntity(User.class);

        assertNotNull(user.getId());
        assertNotNull(user.getName());
        assertNotNull(user.getEmail());
        assertEquals(user.getLevel(), userClaim.getLevel());
        assertEquals(user.getExternalUserId(), externalUserId);

    }

    @DataProvider
    public Object[][] getSchemesAndAlgorithmsForAnonymousAndUser() {
        return schemesAndAlgorithms
            .stream()
            .filter(sp -> sp.scheme.getUserLevel().ordinal() < SUPERUSER.ordinal())
            .map(sp -> new Object[]{sp.scheme, sp.signingAlgorithm, sp.externalUserId})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getSchemesAndAlgorithmsForAnonymousAndUser")
    public void testCustomAuthEscalationIsForbidden(final AuthScheme authScheme,
                                                    final Algorithm algorithm,
                                                    final String externalUserId) {

        final var userClaim = new UserClaim();
        userClaim.setLevel(SUPERUSER);
        userClaim.setExternalUserId(externalUserId);

        final var mapTypeRef = new TypeReference<Map<String, Object>>(){};
        final var userClaimRaw = objectMapper.convertValue(userClaim, mapTypeRef);
        userClaimRaw.entrySet().removeIf(e -> e.getValue() == null);

        final var expiry = currentTimeMillis() + MILLISECONDS.convert(1, HOURS);

        final var jwt = JWT.create()
            .withIssuer(TEST_ISSUER)
            .withAudience(authScheme.getAudience())
            .withExpiresAt(new Date(expiry))
            .withSubject(externalUserId)
            .withClaim(PrivateClaim.AUTH_TYPE.getValue(), CustomJWTCredentials.AUTH_TYPE)
            .withClaim(PrivateClaim.USER_KEY.getValue(), UserKey.EXTERNAL_USER_ID.getValue())
            .withClaim(PrivateClaim.USER.getValue(), userClaimRaw)
            .sign(algorithm);

        final var response = client
            .target(format("%s/user/me", apiRoot))
            .request()
            .header("Authorization", format("Bearer %s", jwt))
            .get();

        assertEquals(response.getStatus(), 403);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(error.getCode(), ErrorCode.FORBIDDEN.toString());

    }

    @DataProvider
    public Object[][] getExternalUserIds() {
        return schemesAndAlgorithms
            .stream()
            .map(st -> new Object[]{st.externalUserId})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getExternalUserIds",
          dependsOnMethods = {"testCustomAuthHappy", "testCustomAuthEscalationIsForbidden"})
    public void testUsersExist(final String externalUserId) {
        final var user = customAuthUserDao.getActiveUser(UserKey.EXTERNAL_USER_ID, externalUserId);
        assertNotNull(user.getId());
        assertNotNull(user.getName());
        assertNotNull(user.getEmail());
        assertEquals(user.getExternalUserId(), externalUserId);
    }

    private static class SchemeTuple {

        public final AuthScheme scheme;

        public final Algorithm signingAlgorithm;

        public final String externalUserId = randomUUID().toString();

        public SchemeTuple(final AuthScheme scheme, final Algorithm signingAlgorithm) {
            this.scheme = scheme;
            this.signingAlgorithm = signingAlgorithm;
        }

    }

}
