//package dev.getelements.elements.service.guice;
//
//import com.auth0.jwt.interfaces.DecodedJWT;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.inject.AbstractModule;
//import com.google.inject.Inject;
//import dev.getelements.elements.dao.AppleSignInSessionDao;
//import dev.getelements.elements.dao.IosApplicationConfigurationDao;
//import dev.getelements.elements.dao.ProfileDao;
//import dev.getelements.elements.model.application.AppleSignInConfiguration;
//import dev.getelements.elements.model.application.Application;
//import dev.getelements.elements.model.application.IosApplicationConfiguration;
//import dev.getelements.elements.model.session.AppleSignInSessionCreation;
//import dev.getelements.elements.model.user.User;
//import dev.getelements.elements.service.NameService;
//import dev.getelements.elements.service.auth.AppleSignInAuthServiceOperations;
//import dev.getelements.elements.service.name.SimpleAdjectiveAnimalNameService;
//import dev.getelements.elements.rt.util.AppleDateFormat;
//import org.testng.annotations.Guice;
//import org.testng.annotations.Test;
//
//import javax.inject.Named;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
//import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
//import static com.google.inject.name.Names.bindProperties;
//import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.APPLE_ITUNES;
//import static dev.getelements.elements.service.auth.AppleSignInAuthServiceOperations.Claim.EMAIL;
//import static dev.getelements.elements.service.auth.AppleSignInAuthServiceOperations.Claim.USER_ID;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@Guice(modules = AppleSignInAuthServiceOperationsTest.Module.class)
//public class AppleSignInAuthServiceOperationsTest {
//
//    private static final String KEY_ID = "test.key.id";
//
//    private static final String TEAM_ID = "test.team.id";
//
//    private static final String CLIENT_ID = "test.client.id";
//
//    private static final String PRIVATE_KEY = "test.private.key";
//
//    private static final String APPLICATION_ID = "test.application.id";
//
//    private static final String IDENTITY_TOKEN = "test.identity.token";
//
//    private static final String AUTHORIZATION_CODE = "test.authorization.code";
//
//    private static final String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
//
//    private static final String PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";
//
//    @Inject
//    private ProfileDao profileDao;
//
//    @Inject
//    private NameService nameService;
//
//    @Inject
//    private AppleSignInSessionDao appleSignInSessionDao;
//
//    @Inject
//    private IosApplicationConfigurationDao iosApplicationConfigurationDao;
//
//    @Inject
//    private AppleSignInAuthServiceOperations underTest;
//
//    @Inject
//    @Named(KEY_ID)
//    private String keyId;
//
//    @Inject
//    @Named(TEAM_ID)
//    private String teamId;
//
//    @Inject
//    @Named(CLIENT_ID)
//    private String clientId;
//
//    @Inject
//    @Named(PRIVATE_KEY)
//    private String privateKey;
//
//    @Inject
//    @Named(APPLICATION_ID)
//    private String applicationId;
//
//    @Inject
//    @Named(IDENTITY_TOKEN)
//    private String identityToken;
//
//    @Inject
//    @Named(AUTHORIZATION_CODE)
//    private String authorizationCode;
//
//    // Jenkins doesn't skip this test for being in a separate group, so I'm modifying this slightly to ignore the
//    // errors. Once we get a more sophisticated build system we'll be able to filter it out.
//    @Test(groups="manual")
//    public void testVerifyAndObtainRefreshToken() {
//
//        reset(iosApplicationConfigurationDao);
//
//        when(profileDao.createOrRefreshProfile(any())).thenAnswer(i -> i.getArgument(0));
//
//        when(iosApplicationConfigurationDao.getIosApplicationConfiguration(
//            eq("test"),
//            eq("testAppId")
//        )).thenAnswer(c -> getAppConfiguration());
//
//        final Function<DecodedJWT, User> mockUserMapper = mock(Function.class);
//        when(mockUserMapper.apply(any())).thenAnswer(p -> {
//            final DecodedJWT appleIdentityToken = p.getArgument(0);
//            final User user = new User();
//            user.setActive(true);
//            user.setEmail(appleIdentityToken.getClaim(EMAIL.value).asString());
//            user.setLevel(User.Level.USER);
//            user.setAppleSignInId(appleIdentityToken.getClaim(USER_ID.value).asString());
//            user.setName(nameService.generateQualifiedName());
//            return user;
//        });
//
//        final AppleSignInSessionCreation appleSignInSessionCreation = underTest
//            .createOrUpdateUserWithAppleSignInTokenAndAuthorizationCode(
//                "test",
//                "testAppId",
//                identityToken,
//                authorizationCode,
//                mockUserMapper
//            );
//
//    }
//
//    private IosApplicationConfiguration getAppConfiguration() {
//
//        final Application application = new Application();
//        final IosApplicationConfiguration appConfiguration = new IosApplicationConfiguration();
//
//        appConfiguration.setParent(application);
//        appConfiguration.setApplicationId(applicationId);
//        appConfiguration.setAppleSignInConfiguration(loadSignInConfiguration());
//
//        return appConfiguration;
//
//    }
//
//    private AppleSignInConfiguration loadSignInConfiguration() {
//        final AppleSignInConfiguration configuration = new AppleSignInConfiguration();
//        configuration.setKeyId(keyId);
//        configuration.setTeamId(teamId);
//        configuration.setClientId(clientId);
//        configuration.setAppleSignInPrivateKey(privateKey);
//        return configuration;
//    }
//
//    public static class Module extends AbstractModule {
//
//        @Override
//        protected void configure() {
//
//            install(new JacksonHttpClientModule()
//                .withRegisteredComponent(OctetStreamJsonMessageBodyReader.class)
//                .withDefaultObjectMapperProvider(() -> {
//                    final ObjectMapper objectMapper = new ObjectMapper();
//                    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
//                    return objectMapper;
//                }).withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
//                    final ObjectMapper objectMapper = new ObjectMapper();
//                    objectMapper.setDateFormat(new AppleDateFormat());
//                    objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
//                    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
//                    return objectMapper;
//                })
//            );
//
//            bind(NameService.class).to(SimpleAdjectiveAnimalNameService.class);
//
//            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
//            bind(AppleSignInSessionDao.class).toInstance(mock(AppleSignInSessionDao.class));
//            bind(IosApplicationConfigurationDao.class).toInstance(mock(IosApplicationConfigurationDao.class));
//
//            // This fixes the private key which will need to have it's newlines added after they were removed.
//            final Map<String, String> env = new HashMap<>(System.getenv());
//
//            if (env.containsKey(PRIVATE_KEY)) {
//
//                final String privateKey = env.get(PRIVATE_KEY).trim()
//                    .replace(PRIVATE_KEY_HEADER, "").trim()
//                    .replace(PRIVATE_KEY_FOOTER, "").trim()
//                    .replaceAll("\\s+", "\n").trim();
//
//                final String fullPrivateKey = new StringBuilder()
//                    .append(PRIVATE_KEY_HEADER).append('\n')
//                    .append(privateKey).append('\n')
//                    .append(PRIVATE_KEY_FOOTER)
//                    .toString();
//
//                env.put(PRIVATE_KEY, fullPrivateKey);
//
//            }
//
//            bindProperties(binder(), env);
//
//        }
//
//    }
//
//}
