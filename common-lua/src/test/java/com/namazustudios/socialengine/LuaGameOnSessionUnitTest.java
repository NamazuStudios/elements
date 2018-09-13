package com.namazustudios.socialengine;

import com.google.inject.Inject;
import com.namazustudios.socialengine.dao.GameOnApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.game.AppBuildType;
import com.namazustudios.socialengine.model.gameon.game.DeviceOSType;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.AMAZON_GAME_ON;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNull;

@Guice(modules = UnitTestModule.class)
public class LuaGameOnSessionUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaGameOnSessionUnitTest.class);

    private Client client;

    private Context context;

    private Application application;

    private GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao;

    @BeforeMethod
    public void resetMocks() {
        reset(getClient(), getApplication(), getGameOnApplicationConfigurationDao());
    }

    @Test(dataProvider = "authResourcesToTest")
    public void performAuthTest(final String moduleName, final String methodName,
                                final DeviceOSType deviceOsType, final AppBuildType buildType) {

        final Path path = new Path("socialengine-test-" + randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create(moduleName, path);

        final Profile profile = new Profile();
        profile.setDisplayName("Testy McTesterson");

        final GameOnApplicationConfiguration gameOnApplicationConfiguration;
        gameOnApplicationConfiguration = spy(GameOnApplicationConfiguration.class);
        gameOnApplicationConfiguration.setCategory(AMAZON_GAME_ON);
        gameOnApplicationConfiguration.setUniqueIdentifier(randomUUID().toString());
        gameOnApplicationConfiguration.setPublicApiKey(randomUUID().toString());

        final WebTarget webTarget = mock(WebTarget.class);
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);

        final Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
        when(webTarget.request()).thenReturn(invocationBuilder);
        when(invocationBuilder.header(anyString(), any())).thenReturn(invocationBuilder);

        final CompletionStageRxInvoker completionStageRxInvoker = mock(CompletionStageRxInvoker.class);
        when(invocationBuilder.rx()).thenReturn(completionStageRxInvoker);

        final Map<String, Object> responseEntity = new HashMap<>();
        responseEntity.put("sessionId", randomUUID().toString());
        responseEntity.put("sessionApiKey", randomUUID().toString());
        responseEntity.put("sessionExpirationDate", new Random().nextInt());

        final CompletionStage<?> completionStage = mock(CompletionStage.class);
        when(completionStageRxInvoker.method(any(), any(Entity.class), any(GenericType.class))).thenReturn(completionStage);
        when(completionStage.exceptionally(any())).thenAnswer(invocation -> completionStage);
        when(completionStage.handleAsync(any())).thenAnswer(invocation -> {

            final BiFunction<Response, Throwable, ?> handler = invocation.getArgument(0);
            final Response response = mock(Response.class);
            final Response.StatusType statusType = mock(Response.StatusType.class);

            when(response.getStatus()).thenReturn(200);
            when(response.getStatusInfo()).thenReturn(statusType);
            when(statusType.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);
            when(response.getHeaders()).thenReturn(mock(MultivaluedMap.class));
            when(response.readEntity(eq(Object.class))).thenReturn(responseEntity);

            final Object result = handler.apply(response, null);
            assertNull(result);

            return completionStage;
        });

        when(getGameOnApplicationConfigurationDao().getDefaultConfigurationForApplication(getApplication().getId()))
            .thenReturn(gameOnApplicationConfiguration);

        final Object result = getContext().getResourceContext().invoke(
            resourceId, methodName,
            profile, deviceOsType, buildType, responseEntity);
        getContext().getResourceContext().destroy(resourceId);

        assertNull(result);
        verify(getClient(), times(1)).target("https://api.amazongameon.com/v1");
        verify(webTarget, times(1)).path("/players/auth");
        verify(invocationBuilder, times(1)).header("x-api-key", gameOnApplicationConfiguration.getPublicApiKey());
        verify(completionStageRxInvoker, times(1)).method(
            matches("POST"),
            argThat((Entity<Object> entity) -> {
                final Map<String, String> request = (Map<String, String>) entity.getEntity();
                return !APPLICATION_JSON.equals(entity.getMediaType()) &&
                       profile.getDisplayName().equals(request.get("playerName")) &&
                       deviceOsType.equals(request.get("deviceOSType")) &&
                       buildType.equals(request.get("appBuildType"));
                }),
            any(GenericType.class));
    }

    @DataProvider
    public static Object[][] authResourcesToTest() {

        final List<Object[]> values = new ArrayList<>();

        for (final DeviceOSType deviceOSType : DeviceOSType.values()) {
            for (final AppBuildType appBuildType : AppBuildType.values()) {
                values.add(new Object[]{
                    "namazu.elements.test.gameon_session", "test_authenticate_session",
                    deviceOSType, appBuildType});
            }
        }

        return values.toArray(new Object[][]{});

    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

    public Application getApplication() {
        return application;
    }

    @Inject
    public void setApplication(Application application) {
        this.application = application;
    }

    public GameOnApplicationConfigurationDao getGameOnApplicationConfigurationDao() {
        return gameOnApplicationConfigurationDao;
    }

    @Inject
    public void setGameOnApplicationConfigurationDao(GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao) {
        this.gameOnApplicationConfigurationDao = gameOnApplicationConfigurationDao;
    }

}
