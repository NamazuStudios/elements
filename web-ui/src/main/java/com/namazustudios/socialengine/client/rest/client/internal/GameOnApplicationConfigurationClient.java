package com.namazustudios.socialengine.client.rest.client.internal;

import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

public interface GameOnApplicationConfigurationClient extends RestService {

    @POST
    @Path("application/{applicationNameOrId}/configuration/game_on")
    void createApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            GameOnApplicationConfiguration gameOnApplicationConfiguration,
            MethodCallback<GameOnApplicationConfiguration> firebaseApplicationConfigurationMethodCallback);

    @GET
    @Path("application/{applicationNameOrId}/configuration/game_on/{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<GameOnApplicationConfiguration> firebaseApplicationConfigurationMethodCallback);

    @PUT
    @Path("application/{applicationNameOrId}/configuration/game_on/{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            GameOnApplicationConfiguration firebaseApplicationConfiguration,
            MethodCallback<GameOnApplicationConfiguration> firebaseApplicationConfigurationMethodCallback);

    @DELETE
    @Path("application/{applicationNameOrId}/configuration/game_on/{applicationProfileNameOrId}")
    void deleteApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
