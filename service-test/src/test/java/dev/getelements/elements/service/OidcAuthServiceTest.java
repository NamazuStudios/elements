package dev.getelements.elements.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.json.gson.GsonFactory;
import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OidcAuthSchemeService;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.sdk.service.util.CryptoKeyPairUtility;
import dev.getelements.elements.service.auth.*;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


public class OidcAuthServiceTest {

    private static final String idToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjliMDI4NWMzMWJmZDhiMDQwZTAzMTU3YjE5YzRlO" +
            "TYwYmRjMTBjNmYiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI4NDAwO" +
            "Dc2MzQ5NzgtZzhqaGRzYWY1N2wxZTRoaWppamdvNDBzbmswZ2ZvMWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQi" +
            "OiI4NDAwODc2MzQ5NzgtZzhqaGRzYWY1N2wxZTRoaWppamdvNDBzbmswZ2ZvMWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20" +
            "iLCJzdWIiOiIxMTExMzU1MzQyNjY5NjA4Nzc3OTkiLCJlbWFpbCI6Iml1cml5LmJ1ZG5pa292QGdtYWlsLmNvbSIsImVtYWlsX3" +
            "ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoidVdMYmN5MTd6Um5Mb00xdnRnRE1NUSIsIm5vbmNlIjoiM1pQR05VSXJSelRWWFlEO" +
            "UZVc2hzdEN6R3pBTnVHTk9XOWVUWVdGdzJOSSIsIm5hbWUiOiJJdXJpaSBCdWRuaWtvdiIsInBpY3R1cmUiOiJodHRwczovL2xo" +
            "My5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NMUEp6TURSMVFNcXFpX0VORUhyUExRX0w3d1JmdGhnSG9DVDJFZFBMRGF" +
            "uMlE9czk2LWMiLCJnaXZlbl9uYW1lIjoiSXVyaWkiLCJmYW1pbHlfbmFtZSI6IkJ1ZG5pa292IiwibG9jYWxlIjoiZW4iLCJpYX" +
            "QiOjE3MDMxNzAxMTAsImV4cCI6MTcwMzE3MzcxMH0.N9y19ItnGFLPlsq9YjhpgRCv_b_C9trN4AZMKmXgCtnPTW25vEiKKK57M" +
            "XuzNB3yWM1cNlCGou0e3uOQ88u4v91rXM84yrMg0LP-gd93SGtDoJ19Suj9n2TAHu90yIDsF6UxBT9Mgm0VJ9T7-ZFYwSv4cnGl" +
            "KFR-ECVpX2ChkMfLqAtMRMKr2pGmFseCWJxG1YH0Wa9l1EA4WSuBFS-I1a-aPSyBINTVAWLBToDJ5AaFUwzB7W4HRdOSN5cjmsA" +
            "b7swPe4kKBl8_q06quEsDRehpElgV7WNMGt6Vv15gShuiHahSFSPb-EX9s9u0tKdJIwGGpHPagbkLeBTosgAj_A";

    private static final String oauthCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDJzCCAg+gAwIBAgIJALiAujtN1gE9MA0GCSqGSIb3DQEBBQUAMDYxNDAyBgNV\n" +
            "BAMMK2ZlZGVyYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20w\n" +
            "HhcNMjMxMjE0MDQzODA5WhcNMjMxMjMwMTY1MzA5WjA2MTQwMgYDVQQDDCtmZWRl\n" +
            "cmF0ZWQtc2lnbm9uLnN5c3RlbS5nc2VydmljZWFjY291bnQuY29tMIIBIjANBgkq\n" +
            "hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuCYe4j3rIDaC9U8jCloiD5UP5cQCndcK\n" +
            "r570LSxEznqNB0qpmtqDJBU+RuSJbMEYZ853AlezSWca8uqDBAgdIWPod+scaQTO\n" +
            "Tg049m9hFwQuP7FzXsAjtxiOHub0nrD60Dy7vI1dPoiyiFdox25JUdW6OSPyq2Ol\n" +
            "FxCPIQy4SpKvebXduA2ZeIY5TWE2wt0mVPo//s9NACn4Ni9GwsPCcgG6yn8oAJ+J\n" +
            "W6xCLnz5/CycNlg178Sxj8LWVEisPbdEK9LhSwQ7V3YU7pfLpEAtGWHYrIcH3+Tf\n" +
            "z6IkS9+UmAzbdjaGk2W+AXkZW8jiIbfNER7e4ZKLntC4Am4InHkJzwIDAQABozgw\n" +
            "NjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDAWBgNVHSUBAf8EDDAKBggr\n" +
            "BgEFBQcDAjANBgkqhkiG9w0BAQUFAAOCAQEAcD1L1nj3evuPrIS1GyZumu1inQEZ\n" +
            "4zqsfMu+q3rxY0eXBZNeT3XMDOvPVyo6rpBYc0A2TOn7KpvxpQK7Db7rB57hhjfX\n" +
            "QT7ZNHBM/bPKYLrWq5uB9CoBm9s61ryu1x5Vnp1HLh1sx19D1lBOUzbhRUQYopyA\n" +
            "0AJC/dj/OaH0SY1EsoWNq5/IW5DrTghy45U9VixI4sMNwtuUJgfFF0cCBVtkmJh7\n" +
            "c5NRQw4CfbYb/E5akV9UPkPc4RRrMPA1oAN1ZvRuvyO18nwNIVwg98KzEVTD/5BX\n" +
            "caVxabmVvnP5AEENQUyLDVfyV2QCozIM0UZisDwvcRfUZb7EqjVk2Aq/+A==\n" +
            "-----END CERTIFICATE-----\n";

    private static final String sessionSecret = "1234abcd";

    private static final Logger logger = LoggerFactory.getLogger(OidcAuthServiceTest.class);

    private final String userId = randomUUID().toString();

    private final String applicationId = randomUUID().toString();

    @Inject
    private MapperRegistry mapperRegistry;

    @Inject
    private Provider<UserOidcAuthService> userOidcAuthServiceProvider;

    @Inject
    private Provider<AnonOidcAuthService> anonOidcAuthServiceProvider;

    @Inject
    private SessionDao oidcSessionDao;

    @Inject
    private UserDao oidcUserDao;

    @Inject
    private OidcAuthSchemeDao oidcAuthSchemeDao;

    @Inject
    ApplicationDao applicationDao;

    private Application application;

    @BeforeClass
    public void setup() {
        final var injector = createInjector(new TestModule());
        injector.injectMembers(this);

        application = new Application();
        application.setName("googlesigninapplication");
        application.setId(applicationId);

        when(oidcAuthSchemeDao.getAuthScheme(any(String.class))).then(i -> {

            final var scheme = new OidcAuthScheme();
            final var pubKey = (RSAPublicKey) GoogleSignInCertificateHelper.certToPublicKey(oauthCert);
            final var key = new JWK(
                    "RS256",
                    "9b0285c31bfd8b040e03157b19c4e960bdc10c6f",
                    "RSA",
                    "sig",
                    Base64.getUrlEncoder().encodeToString(pubKey.getPublicExponent().toByteArray()),
                    Base64.getUrlEncoder().encodeToString(pubKey.getModulus().toByteArray()));


            scheme.setName("Google");
            scheme.setIssuer("https://accounts.google.com");
            scheme.setKeysUrl("https://www.googleapis.com/oauth2/v3/certs");
            scheme.setMediaType("application/json");
            scheme.setKeys(List.of(key));

            return scheme;
        });

    }

    @BeforeMethod
    public void resetMocks() {
        reset(oidcSessionDao, oidcUserDao, applicationDao);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testAnonExpiredToken() {
        final var toTest = anonOidcAuthServiceProvider.get();
        final var sessionRequest = new OidcSessionRequest();
        sessionRequest.setJwt(idToken);

        toTest.createSession(sessionRequest);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testUserExpiredToken() {
        final var toTest = userOidcAuthServiceProvider.get();
        final var sessionRequest = new OidcSessionRequest();
        sessionRequest.setJwt(idToken);

        toTest.createSession(sessionRequest);
    }

    @Test
    public void testVerifyUserToken() {

        when(oidcUserDao.createUser(any(User.class))).then(i ->{
            final var output = mapperRegistry.map(i.getArgument(0), User.class);
            output.setId(userId);
            return output;
        });

        when(oidcSessionDao.create(any(Session.class))).then(i -> {
            final var input = mapperRegistry.map(i.getArgument(0), Session.class);
            final var output = new SessionCreation();
            output.setSession(input);
            output.setSessionSecret(sessionSecret);
            return output;
        });

        doTest();
    }

    private void doTest() {

        try {
            final var creation = createSession();

            assertNotNull(creation.getSessionSecret(), "Session secret not generated");

            assertNotNull(creation);
            assertEquals(sessionSecret, creation.getSessionSecret());

            final var session = creation.getSession();
            assertNotNull(session);

            assertEquals(session.getApplication(), application);
            assertTrue(session.getExpiry() > currentTimeMillis(), "Session future expiry.");

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private SessionCreation createSession() throws CertificateException, IOException {

        final var decodedToken = JWT.decode(idToken);
        final var key = (RSAPublicKey) GoogleSignInCertificateHelper.certToPublicKey(oauthCert);
        final var algorithm = Algorithm.RSA256(key, null);

        // Will throw a SignatureVerificationException if the token's signature is invalid
        algorithm.verify(decodedToken);

        final var parsedToken = GoogleIdToken.parse(GsonFactory.getDefaultInstance(), idToken);
        final var user = mapTokenToUser(parsedToken);
        final var activeUser = oidcUserDao.createUser(user);
        final var session = new Session();
        session.setUser(activeUser);
        session.setApplication(application);
        session.setExpiry(new Date().getTime() + 10000);

        return oidcSessionDao.create(session);
    }

    private User mapTokenToUser(final GoogleIdToken identityToken) {

        final var payload = identityToken.getPayload();
        final var userId = payload.getSubject();
        final var email = payload.getEmail();
        final var name = (String) payload.get("name");
        final var user = new User();

        user.setLevel(USER);
        user.setName(name);
        user.setEmail(email);
        user.setId(userId);

        return user;
    }

    @DataProvider
    public Object[][] allAuthServices() {
        return new Object[][] {
                {
                    anonOidcAuthServiceProvider.get(),
                    userOidcAuthServiceProvider.get()
                }
        };
    }

    public class TestModule extends AbstractModule {

        @Override
        protected void configure() {

            bind(Client.class).toInstance(mock(Client.class));
            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(NameService.class).toInstance(mock(NameService.class));
            bind(ApplicationDao.class).toInstance(mock(ApplicationDao.class));
            bind(UserDao.class).toInstance(mock(UserDao.class));
            bind(UserUidDao.class).toInstance(mock(UserUidDao.class));
            bind(SessionDao.class).toInstance(mock(SessionDao.class));
            bind(OidcAuthSchemeDao.class).toInstance(mock(OidcAuthSchemeDao.class));
            bind(CryptoKeyPairUtility.class).toInstance(mock(CryptoKeyPairUtility.class));
            bind(OidcAuthSchemeService.class).toInstance(mock(OidcAuthSchemeService.class));

            // Service Level Dependencies
            bind(MapperRegistry.class).toProvider(ServicesMapperRegistryProvider.class);
            bind(long.class).annotatedWith(named(SESSION_TIMEOUT_SECONDS)).toInstance(300L);
            bind(String.class).annotatedWith(named(API_OUTSIDE_URL)).toInstance("http://localhost:8080/api/rest");
        }

    }

}
