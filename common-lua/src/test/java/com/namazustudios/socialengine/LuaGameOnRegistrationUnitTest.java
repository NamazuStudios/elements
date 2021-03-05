package com.namazustudios.socialengine;

import com.google.inject.Inject;
import com.namazustudios.socialengine.dao.GameOnApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.GameOnRegistrationDao;
import com.namazustudios.socialengine.exception.gameon.GameOnRegistrationNotFoundException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.game.AppBuildType;
import com.namazustudios.socialengine.model.gameon.game.DeviceOSType;
import com.namazustudios.socialengine.model.gameon.game.GameOnRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.testng.annotations.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import static com.namazustudios.socialengine.TestUtils.getUnixFSTest;
import static com.namazustudios.socialengine.TestUtils.getXodusTest;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.AMAZON_GAME_ON;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNull;

public class LuaGameOnRegistrationUnitTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                getXodusTest(LuaGameOnRegistrationUnitTest.class),
                getUnixFSTest(LuaGameOnRegistrationUnitTest.class)
        };
    }

    private Client client;

    private Context context;

    private Application application;

    private GameOnRegistrationDao gameOnRegistrationDao;

    private GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao;

    @BeforeMethod
    public void resetMocks() {
        reset(getClient(),
              getApplication(),
              getGameOnRegistrationDao(),
              getGameOnApplicationConfigurationDao());
    }

    @Test
    public void performRefreshSuccessTest() {

        final Path path = new Path("socialengine-test-" + randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("namazu.elements.test.gameon_registration", path);

        final Profile profile = new Profile();
        profile.setId(randomUUID().toString());
        profile.setDisplayName("Testy McTesterson");

        final GameOnApplicationConfiguration gameOnApplicationConfiguration;
        gameOnApplicationConfiguration = spy(GameOnApplicationConfiguration.class);
        gameOnApplicationConfiguration.setCategory(AMAZON_GAME_ON);
        gameOnApplicationConfiguration.setUniqueIdentifier(randomUUID().toString());
        gameOnApplicationConfiguration.setPublicApiKey(randomUUID().toString());
        when(getGameOnApplicationConfigurationDao().getDefaultConfigurationForApplication(getApplication().getId()))
            .thenReturn(gameOnApplicationConfiguration);

        final GameOnRegistration gameOnRegistration;
        gameOnRegistration = new GameOnRegistration();
        gameOnRegistration.setProfile(profile);
        gameOnRegistration.setPlayerToken(randomUUID().toString());
        gameOnRegistration.setExternalPlayerId(randomUUID().toString());
        when(getGameOnRegistrationDao().getRegistrationForProfile(profile)).thenReturn(gameOnRegistration);

        final Object result = getContext().getResourceContext().invoke(
                resourceId, "test_refresh_registration",
                profile, gameOnRegistration);
        getContext().getResourceContext().destroy(resourceId);

        verify(getGameOnRegistrationDao(), times(1)).getRegistrationForProfile(profile);

    }

    @Test
    public void performRefreshFailureTest() {

        final Path path = new Path("socialengine-test-" + randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("namazu.elements.test.gameon_registration", path);

        final Profile profile = new Profile();
        profile.setId(randomUUID().toString());
        profile.setDisplayName("Testy McTesterson");

        final GameOnApplicationConfiguration gameOnApplicationConfiguration;
        gameOnApplicationConfiguration = spy(GameOnApplicationConfiguration.class);
        gameOnApplicationConfiguration.setCategory(AMAZON_GAME_ON);
        gameOnApplicationConfiguration.setUniqueIdentifier(randomUUID().toString());
        gameOnApplicationConfiguration.setPublicApiKey(randomUUID().toString());

        final RequestMocks requestMocks = new RequestMocks(gameOnApplicationConfiguration);
        final Map<String, Object> responseEntity = requestMocks.setupMocks();

        final GameOnRegistration gameOnRegistration;
        gameOnRegistration = new GameOnRegistration();
        gameOnRegistration.setProfile(profile);
        gameOnRegistration.setPlayerToken((String) responseEntity.get("playerToken"));
        gameOnRegistration.setExternalPlayerId((String) responseEntity.get("externalPlayerId"));
        when(getGameOnRegistrationDao().getRegistrationForProfile(profile))
            .thenThrow(new GameOnRegistrationNotFoundException());

        when(getGameOnApplicationConfigurationDao().getDefaultConfigurationForApplication(getApplication().getId()))
            .thenReturn(gameOnApplicationConfiguration);

        final Object result = getContext().getResourceContext().invoke(
                resourceId, "test_refresh_registration",
                profile, responseEntity);
        getContext().getResourceContext().destroy(resourceId);
        assertNull(result);

        requestMocks.verifyRequest();
        verify(getGameOnRegistrationDao(), times(1)).createRegistration(eq(gameOnRegistration));

    }

    @DataProvider
    public static Object[][] testData() {

        final List<Object[]> values = new ArrayList<>();

        for (final DeviceOSType deviceOSType : DeviceOSType.values()) {
            for (final AppBuildType appBuildType : AppBuildType.values()) {
                values.add(new Object[]{deviceOSType, appBuildType});
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

    public GameOnRegistrationDao getGameOnRegistrationDao() {
        return gameOnRegistrationDao;
    }

    @Inject
    public void setGameOnRegistrationDao(GameOnRegistrationDao gameOnRegistrationDao) {
        this.gameOnRegistrationDao = gameOnRegistrationDao;
    }

    public GameOnApplicationConfigurationDao getGameOnApplicationConfigurationDao() {
        return gameOnApplicationConfigurationDao;
    }

    @Inject
    public void setGameOnApplicationConfigurationDao(GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao) {
        this.gameOnApplicationConfigurationDao = gameOnApplicationConfigurationDao;
    }

    private class RequestMocks {

        private final GameOnApplicationConfiguration gameOnApplicationConfiguration;

        public RequestMocks(final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
            this.gameOnApplicationConfiguration = gameOnApplicationConfiguration;
        }

        private final WebTarget webTarget = mock(WebTarget.class);

        private final Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);

        private final CompletionStageRxInvoker completionStageRxInvoker = mock(CompletionStageRxInvoker.class);

        public Map<String, Object> setupMocks() {

            when(client.target(anyString())).thenReturn(webTarget);
            when(webTarget.path(anyString())).thenReturn(webTarget);
            when(webTarget.request()).thenReturn(invocationBuilder);
            when(invocationBuilder.header(anyString(), any())).thenReturn(invocationBuilder);
            when(invocationBuilder.rx()).thenReturn(completionStageRxInvoker);

            final Map<String, Object> responseEntity = new HashMap<>();
            responseEntity.put("playerToken", randomUUID().toString());
            responseEntity.put("externalPlayerId", randomUUID().toString());

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

            return responseEntity;

        }

        public void verifyRequest() {
            verify(getClient(), times(1)).target("https://api.amazongameon.com/v1");
            verify(webTarget, times(1)).path("/players/register");
            verify(invocationBuilder, times(1)).header("x-api-key", gameOnApplicationConfiguration.getPublicApiKey());
            verify(completionStageRxInvoker, times(1)).method(
                matches("POST"),
                argThat((Entity<Object> entity) -> !APPLICATION_JSON.equals(entity.getMediaType())),
                any(GenericType.class));

        }
    }

}
