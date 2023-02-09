package com.namazustudios.socialengine.service;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.FirebaseUserDao;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.model.session.FirebaseSessionRequest;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.auth.AnonFirebaseAuthService;
import com.namazustudios.socialengine.service.auth.UserFirebaseAuthService;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;


public class FirebaseAuthServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthServiceTest.class);

    private final String userId = randomUUID().toString();

    private final String configurationId = randomUUID().toString();

    private final FirebaseTestClient ftc = new FirebaseTestClient();

    private FirebaseEmailPasswordSignUpResponse signupResult;

    private FirebaseUsernamePasswordSignInResponse signinResult;

    /* Ths is a valid, but expired firebase token. Used to ensure an expired token gets the correct exception. */

    private static final String EXPIRED_FIREBASE_TOKEN =
        "eyJhbGciOiJSUzI1NiIsImtpZCI6IjhkOGM3OTdlMDQ5YWFkZWViOWM" +
        "5M2RiZGU3ZDAwMzJmNjk3NjYwYmQiLCJ0eXAiOiJKV1QifQ.eyJpc3M" +
        "iOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZWxlbWVudH" +
        "MtaW50ZWdyYXRpb24tdGVzdCIsImF1ZCI6ImVsZW1lbnRzLWludGVnc" +
        "mF0aW9uLXRlc3QiLCJhdXRoX3RpbWUiOjE2MTczMjU5NTYsInVzZXJf" +
        "aWQiOiJkUzMzcG5QWU12WTVySnhUQWhURmRzWE9sZ0oyIiwic3ViIjo" +
        "iZFMzM3BuUFlNdlk1ckp4VEFoVEZkc1hPbGdKMiIsImlhdCI6MTYxNz" +
        "MyNTk1NiwiZXhwIjoxNjE3MzI5NTU2LCJlbWFpbCI6InRlc3R5Lm1jd" +
        "GVzdGVyc29uLmY5N2NiZmExLWQ1OTktNDQzMy04MzgwLWZmYmVmOTE2" +
        "NzhkYUBuYW1henVzdHVkaW9zLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp" +
        "mYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6Wy" +
        "J0ZXN0eS5tY3Rlc3RlcnNvbi5mOTdjYmZhMS1kNTk5LTQ0MzMtODM4M" +
        "C1mZmJlZjkxNjc4ZGFAbmFtYXp1c3R1ZGlvcy5jb20iXX0sInNpZ25f" +
        "aW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.UmTX8Zzne9Yd5-SGOa-u" +
        "Dcm-w4VHiUgEGTRPOscFcE-F0y9p7X3LNzIM8WP4JdJZpjjkblaQdrk" +
        "yzjxb3QoTh_03d8CbB8Pkj6Fbm3y4t3s7aZHzHQwTHkgA79uZ_qCLk_" +
        "aeabdYYH0XaIjGRmLuAW8zQt1P4DCn8ytYx1-dB_9KyHCRvdwqcKdTI" +
        "pDQ96x5Sose7algQgeVZam4bBws_5xiIc3Ej9TA7e-E8k46Itl-b5YV" +
        "9zbMsgbGcJb8plnNWNyQMUwr0RMnpmw7Jondlw_i6DCCNzh38y_NtoR" +
        "DhgzaYtVoHIF3ErFtq2P2IHgSWtRvzUFqOZhzTFWW0HSklQ";

    @Inject
    private Mapper mapper;

    @Inject
    private Provider<AnonFirebaseAuthService> anonFirebaseAuthServiceProvider;

    @Inject
    private Provider<UserFirebaseAuthService> userFirebaseAuthServiceProvider;

    @Inject
    private SessionDao sessionDao;

    @Inject
    private FirebaseUserDao firebaseUserDao;

    @Inject
    private FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao;

    @BeforeClass
    public void signupUser() {

        final var injector = createInjector(new TestModule());
        injector.injectMembers(this);

        final var signup = ftc.randomUserSignup();
        signupResult = ftc.signUp(signup);
        logger.info("Successfully created user.");

        final var signin = new FirebaseUsernamePasswordSignInRequest(signup);
        signinResult = ftc.signIn(signin);
        logger.info("Successfully logged-in user.");

    }

    @BeforeMethod
    public void resetMocks() {
        reset(sessionDao, firebaseUserDao, firebaseApplicationConfigurationDao);
    }

    @Test
    public void testVerifyAnon() {

        when(firebaseUserDao.createReactivateOrUpdateUser(any(User.class))).then(i ->{
            final var output = mapper.map(i.getArgument(0), User.class);
            output.setId(userId);
            output.setActive(true);
            return output;
        });

        final var toTest = anonFirebaseAuthServiceProvider.get();
        doTest(toTest);

    }

    @Test(dependsOnMethods = "testVerifyAnon")
    public void testVerifyUser() {

        when(firebaseUserDao.connectActiveUserIfNecessary(any(User.class))).then(i ->{
            final var output = mapper.map(i.getArgument(0), User.class);
            output.setId(userId);
            output.setActive(true);
            return output;
        });

        final var toTest = userFirebaseAuthServiceProvider.get();
        doTest(toTest);

    }

    private void doTest(final FirebaseAuthService firebaseAuthService) {

        final var sessionSecret = randomUUID().toString();
        final var applicationId = randomUUID().toString();

        final var app = new Application();
        app.setId(applicationId);
        app.setName("TEST");
        app.setDescription("Test Application");

        when(firebaseApplicationConfigurationDao.getApplicationConfiguration(matches("elements-integration-test"))).then(a -> {

             final var conf = new FirebaseApplicationConfiguration();
             conf.setId(configurationId);
             conf.setServiceAccountCredentials(FirebaseServiceAccountCredentials.loadServiceAccountCredentials());
             conf.setProjectId("elements-integration-test");
             conf.setParent(app);

             return conf;

         });

        when(sessionDao.create(any(Session.class))).then(a -> {

            final var input = (Session) a.getArgument(0);

            final var output = new Session();
            output.setExpiry(input.getExpiry());
            output.setUser(input.getUser());
            output.setProfile(input.getProfile());
            output.setApplication(input.getApplication());

            final var creation = new SessionCreation();
            creation.setSession(output);
            creation.setSessionSecret(sessionSecret);

            return creation;

        });

        final var request = new FirebaseSessionRequest();
        request.setFirebaseJWT(signinResult.getIdToken());
        final var creation = firebaseAuthService.createOrUpdateUserWithFirebaseJWT(request);

        assertNotNull(creation);
        assertEquals(creation.getSessionSecret(), sessionSecret);

        final var session = creation.getSession();
        assertNotNull(session);

        final var expected = getSignedInUser();
        expected.setFirebaseId(signinResult.getLocalId());
        assertEquals(session.getUser(), expected);

        assertEquals(session.getApplication(), app);
        assertTrue(session.getExpiry() > currentTimeMillis(), "Session future expiry.");

    }

    @DataProvider
    public Object[][] allAuthServices() {
        return new Object[][] {
            { anonFirebaseAuthServiceProvider.get() },
            { userFirebaseAuthServiceProvider.get() }
        };
    }

    @Test(dependsOnMethods = "testVerifyUser",
          dataProvider = "allAuthServices",
          expectedExceptions = ForbiddenException.class)
    public void testExpiredToken(final FirebaseAuthService firebaseAuthService) {
        final FirebaseSessionRequest req = new FirebaseSessionRequest();
        req.setFirebaseJWT(EXPIRED_FIREBASE_TOKEN);
        firebaseAuthService.createOrUpdateUserWithFirebaseJWT(req);
    }

    @AfterClass
    public void destroyAccount() {
        final var request = new FirebaseDeleteAccountRequest();
        request.setIdToken(signinResult.getIdToken());
        ftc.deleteAccount(request);
        logger.info("Successfully deleted account.");
    }

    private User getSignedInUser() {
        final var user = new User();
        user.setId(userId);
        user.setActive(true);
        user.setLevel(User.Level.USER);
        user.setEmail(signupResult.getEmail());
        user.setName(signinResult.getLocalId());
        return user;
    }

    public class TestModule extends AbstractModule {

        @Override
        protected void configure() {

            final var sessionDao = mock(SessionDao.class);
            bind(SessionDao.class).toInstance(sessionDao);

            final var firebaseUserDao = mock(FirebaseUserDao.class);
            bind(FirebaseUserDao.class).toInstance(firebaseUserDao);

            final var firebaseApplicationConfigurationDao = mock(FirebaseApplicationConfigurationDao.class);
            bind(FirebaseApplicationConfigurationDao.class).toInstance(firebaseApplicationConfigurationDao);

            install(new FirebaseAppFactoryModule());

            // Service Level Dependencies
            bind(Mapper.class).toProvider(ServicesDozerMapperProvider.class);
            bind(User.class).toProvider(FirebaseAuthServiceTest.this::getSignedInUser);
            bind(long.class).annotatedWith(named(SESSION_TIMEOUT_SECONDS)).toInstance(300l);

        }

    }

}
