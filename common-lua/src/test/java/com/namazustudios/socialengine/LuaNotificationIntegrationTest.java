package com.namazustudios.socialengine;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.service.Notification;
import com.namazustudios.socialengine.service.NotificationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;

@Guice(modules = UnitTestModule.class)
public class LuaNotificationIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaNotificationIntegrationTest.class);

    private Context context;

    private NotificationBuilder mockNotificationBuilder;

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
        when(getMockNotificationBuilder().add(anyString(), anyString())).thenReturn(getMockNotificationBuilder());
        when(getMockNotificationBuilder().addAll(anyMap())).thenReturn(getMockNotificationBuilder());
        when(getMockNotificationBuilder().build()).thenReturn(mockNotification);

        performLuaTest("namazu.elements.test.notification","test_send_with_builder", attributes);

        verify(mockNotification, times(1)).send();
        verify(getMockNotificationBuilder(), times(1)).title(eq("Hello World!"));
        verify(getMockNotificationBuilder(), times(1)).message(eq("Hello World!"));
        verify(getMockNotificationBuilder(), times(1)).application(eq(mockApplication));
        verify(getMockNotificationBuilder(), times(1)).recipient(eq(mockProfile));
        verify(getMockNotificationBuilder(), times(1)).add(eq("single"), eq("property"));

        Map<String,String> allProps = new HashMap<>();
        allProps.put("extraA","foo");
        allProps.put("extraB","bar");
        verify(getMockNotificationBuilder(), times(1)).addAll(eq(allProps));

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
