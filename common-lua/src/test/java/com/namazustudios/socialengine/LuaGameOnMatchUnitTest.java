package com.namazustudios.socialengine;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertNull;

@Guice(modules = UnitTestModule.class)
public class LuaGameOnMatchUnitTest {

    private  static final int SCORE = 42;

    private Client client;

    private Context context;

    @Test
    public void testSubmitScore() {

        final Path path = new Path("socialengine-test-" + randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("namazu.elements.test.gameon_match", path);

        final String matchId = randomUUID().toString();
        final String sessionId = randomUUID().toString();
        final String sessionApiKey = randomUUID().toString();
        final WebTarget webTarget = mock(WebTarget.class);
        final Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
        final CompletionStageRxInvoker completionStageRxInvoker = mock(CompletionStageRxInvoker.class);

        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(invocationBuilder);
        when(invocationBuilder.header(anyString(), any())).thenReturn(invocationBuilder);
        when(invocationBuilder.rx()).thenReturn(completionStageRxInvoker);

        final Map<String, Object> responseEntity = new HashMap<>();
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

        final Object result = getContext().getResourceContext().invoke(
                resourceId, "test_post_score",
                sessionId, sessionApiKey, matchId, SCORE);

        getContext().getResourceContext().destroy(resourceId);
        assertNull(result);

        verify(invocationBuilder, times(1)).header(eq("session-id"), eq(sessionId));
        verify(invocationBuilder, times(1)).header(eq("x-api-key"), eq(sessionApiKey));
        verify(getClient(), times(1)).target("https://api.amazongameon.com/v1");
        verify(webTarget, times(1)).path("/matches/" + matchId + "/score");
        verify(completionStageRxInvoker, times(1)).method(
            matches("PUT"),
            argThat((Entity<Object> entity) -> {
                final Map<String, Object> request = (Map<String, Object>) entity.getEntity();
                final int score = ((Number)request.get("score")).intValue();
                return !APPLICATION_JSON.equals(entity.getMediaType()) && score == SCORE;
            }),
            any(GenericType.class));

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

}
