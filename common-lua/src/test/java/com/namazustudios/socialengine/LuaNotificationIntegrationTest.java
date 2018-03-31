package com.namazustudios.socialengine;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQClientModule;
import com.namazustudios.socialengine.service.Notification;
import com.namazustudios.socialengine.service.NotificationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.inject.Guice.createInjector;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;

public class LuaNotificationIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaNotificationIntegrationTest.class);

    private Context context;

    private NotificationBuilder mockNotificationBuilder;

    @BeforeClass
    public void initializeGuice() {

        final NotificationBuilder mockNotificationBuilder = mock(NotificationBuilder.class);
        final IntegrationTestModule integrationTestModule = new IntegrationTestModule();

        integrationTestModule.getEmbeddedTestService()
            .withNodeModule(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(NotificationBuilder.class).toInstance(mockNotificationBuilder);
                }
            });

        final Injector injector = Guice.createInjector(integrationTestModule, new AbstractModule() {
            @Override
            protected void configure() {
                bind(NotificationBuilder.class).toInstance(mockNotificationBuilder);
            }
        });

        injector.injectMembers(this);

    }

    @BeforeMethod
    public void resetMocks() {
        reset(getMockNotificationBuilder());
    }

    @Test
    public void testSendWithBuilder() throws Exception {

        final Profile mockProfile = new Profile();
        final Application mockApplication = new Application();

        mockApplication.setId(randomUUID().toString());
        mockApplication.setName("Test App");
        mockProfile.setApplication(mockApplication);
        mockProfile.setDisplayName("Testy McTesterson");

        final Attributes attributes = new SimpleAttributes.Builder()
            .setAttribute(Profile.PROFILE_ATTRIBUTE, mockProfile)
            .setAttribute(Application.APPLICATION_ATTRIUTE, mockApplication)
            .build();

        final Notification mockNotification = mock(Notification.class);
        when(getMockNotificationBuilder().application(any())).thenReturn(getMockNotificationBuilder());
        when(getMockNotificationBuilder().recipient(any())).thenReturn(getMockNotificationBuilder());
        when(getMockNotificationBuilder().message(any())).thenReturn(getMockNotificationBuilder());
        when(getMockNotificationBuilder().title(any())).thenReturn(getMockNotificationBuilder());
        when(getMockNotificationBuilder().build()).thenReturn(mockNotification);

        performLuaTest("namazu.socialengine.test.notification","test_send_with_builder", attributes);

        verify(mockNotification, times(1)).send();
        verify(getMockNotificationBuilder(), times(1)).title(eq("Hello World!"));
        verify(getMockNotificationBuilder(), times(1)).message(eq("Hello World!"));
        verify(getMockNotificationBuilder(), times(1)).application(eq(mockApplication));
        verify(getMockNotificationBuilder(), times(1)).recipient(eq(mockProfile));

    }

    public void performLuaTest(final String moduleName,
                               final String methodName,
                               final Attributes attributes) throws InterruptedException {
        final Path path = new Path("socialengine-auth-test-" + randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().createAttributes(moduleName, path, attributes);
        final Object result = getContext().getResourceContext().invoke(resourceId, methodName);
        logger.info("Successfuly got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

    public NotificationBuilder getMockNotificationBuilder() {
        return mockNotificationBuilder;
    }

    @Inject
    public void setMockNotificationBuilder(NotificationBuilder mockNotificationBuilder) {
        this.mockNotificationBuilder = mockNotificationBuilder;
    }

}
