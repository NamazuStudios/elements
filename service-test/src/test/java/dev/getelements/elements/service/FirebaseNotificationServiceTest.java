package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.FirebaseApplicationConfiguration;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.sdk.service.notification.NotificationBuilder;
import dev.getelements.elements.sdk.service.notification.NotificationDestinationFactory;
import dev.getelements.elements.sdk.service.notification.NotificationFactory;
import dev.getelements.elements.sdk.service.notification.NotificationService;
import dev.getelements.elements.service.firebase.CachingFirebaseAppFactory;
import dev.getelements.elements.service.firebase.FirebaseAppFactory;
import dev.getelements.elements.service.guice.StandardNotificationFactoryProvider;
import dev.getelements.elements.service.notification.StandardNotificationBuilder;
import dev.getelements.elements.service.notification.StandardNotificationDestinationFactoryProvider;
import dev.getelements.elements.service.notification.StandardNotificationService;
import dev.getelements.elements.service.notification.firebase.FirebaseMessagingFactory;
import dev.getelements.elements.service.notification.firebase.FirebaseMessagingFactoryProvider;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Supplier;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FirebaseNotificationServiceTest {

    @Inject
    private NotificationService notificationService;

    @Inject
    private UserDao userDao;

    @Inject
    private ProfileDao profileDao;

    @Inject
    private Supplier<Profile> profileSupplier;

    @Inject
    private ApplicationConfigurationDao configurationDao;

    private Application application;

    private static final String SERVICE_CREDENTIALS = """
            {
              "type": "service_account",
              "project_id": "test-13cde",
              "private_key_id": "121add275a550e109b890c46714b3a5813b74bxa",
              "private_key": "
-----BEGIN PRIVATE KEY-----
MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIckBwU7sBh3cSic
IerQYtmeXyYxJ8MPJItnievt35GVwFZcAHaJgrRrzJ3oAnRqUfeEz/dRrENpnkCW
jFz2V3qRndYl0UW9YKaANJnUWFKKnmmerFdClizUG+AfRg9kUiC5AR6kErfF7cfW
vSqa8HmlarU8mNll53RjlGqeacqBAgMBAAECgYEAgx6mnSI7ipGhPni2w+DqeCEc
Sq8Y2D5CA+wyurHy4It+sYvUey6SncGWJ4Orlg5udwzqxxj8XfAFNr+bcrslRfQk
kEe2BOaVGPw8CCKybEvLkpp0bd0tKPQTkOVvQbxos57y3YaO6i2eajpU8JCr2oTH
d3OcCwL8YMT6aBvACZECQQD4LErhU0I0no3noLkMualKE4gzD4rGWwocsP+C2niz
tl/Wv0QH7vvpP4Gnrs841XhWWnmqybYL9FrAsQMmNR0FAkEAi2chkX//A79z52Xx
s7mjpq6hMJRUsqZrXkziSjN6VQw9PVz7rXg7b2WsnSNFuJLk3eSmiRhMuvMzp548
zCzQTQJBANwVM4aldlYBsiiSsOinEYk+zw8xHkmP3OTa1o0tv9LnzhA8aFF8z2vn
RMz2ypBFR0WVUMZzwXzSg1TU5c0RVx0CQDBVcsMjEQV4pTQvGY3KKN7LH6JbW76R
ixqoJ7G4hYrlcnpLOgwcaucl5qKZLzxe2jHBbKiOH1SWrGtKpyPXyeUCQCgX8AyS
KlteRzkltIjfHATcNP0Oh4f79Iqwk13eFN6I/6DqyUwTEV2l2fWoyK+8rl5KLrKn
glLgpf1FI4IkLG8=
-----END PRIVATE KEY-----",
              "client_email": "firebase-adminsdk-tgpvq@word-rally-13cde.iam.gserviceaccount.com",
              "client_id": "102229861885471512216",
              "auth_uri": "https://accounts.google.com/o/oauth2/auth",
              "token_uri": "https://oauth2.googleapis.com/token",
              "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
              "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-tgpvq%40word-rally-13cde.iam.gserviceaccount.com",
              "universe_domain": "googleapis.com"
            }
            """;

    @BeforeClass
    public void setup() {
        final var injector = createInjector(new TestModule());
        injector.injectMembers(this);

        application = new Application();
        application.setName("test_application_notifications");
        application.setId("test_application_notifications_id");

        when(userDao.getUser(anyString())).then(a -> mock(User.class));
        when(profileDao.getActiveProfile(anyString())).then(a -> mock(Profile.class));
        when(profileSupplier.get()).then(a -> profileDao.getActiveProfile(anyString()));
        when(configurationDao.getDefaultApplicationConfigurationForApplication(
                any(),
                any()
        )).then(a -> {
            final var appConfig = new FirebaseApplicationConfiguration();
            appConfig.setId(application.getId());
            appConfig.setParent(application);
            appConfig.setName("test_application_notifications_config");
            appConfig.setDescription("test_application_notifications_config_description");
            appConfig.setServiceAccountCredentials(SERVICE_CREDENTIALS);
            return appConfig;
        });
    }

    @Test
    public void testBuildNotification() {

        final var sender = profileDao.getActiveProfile("sender");
        final var receiver = profileDao.getActiveProfile("receiver");

        notificationService.getBuilder()
                .sender(sender)
                .recipient(receiver)
                .application(application)
                .message("test notification message")
                .title("test notification title")
                .build()
                .send();
    }


    public class TestModule extends AbstractModule {

        @Override
        protected void configure() {

            bind(Client.class).toInstance(mock(Client.class));
            bind(UserDao.class).toInstance(mock(UserDao.class));
            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(NameService.class).toInstance(mock(NameService.class));
            bind(ApplicationDao.class).toInstance(mock(ApplicationDao.class));
            bind(FCMRegistrationDao.class).toProvider(() -> mock(FCMRegistrationDao.class));
            bind(ApplicationConfigurationDao.class).toInstance(mock(ApplicationConfigurationDao.class));

            bind(NotificationService.class).to(StandardNotificationService.class);
            bind(NotificationService.class).annotatedWith(named(UNSCOPED)).to(StandardNotificationService.class);

            bind(FirebaseAppFactory.class).to(CachingFirebaseAppFactory.class);
            bind(NotificationBuilder.class).to(StandardNotificationBuilder.class);
            bind(NotificationFactory.class).toProvider(StandardNotificationFactoryProvider.class);
            bind(FirebaseMessagingFactory.class).toProvider(FirebaseMessagingFactoryProvider.class);
            bind(NotificationDestinationFactory.class).toProvider(StandardNotificationDestinationFactoryProvider.class);

            bind(new TypeLiteral<Supplier<Profile>>(){}).toInstance(mock(Supplier.class));

            // Service Level Dependencies
            bind(MapperRegistry.class).toProvider(ServicesMapperRegistryProvider.class);
            bind(long.class).annotatedWith(named(SESSION_TIMEOUT_SECONDS)).toInstance(300L);
            bind(String.class).annotatedWith(named(API_OUTSIDE_URL)).toInstance("http://localhost:8080/api/rest");
        }

    }
}

