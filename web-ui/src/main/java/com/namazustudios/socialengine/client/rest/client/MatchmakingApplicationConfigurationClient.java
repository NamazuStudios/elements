package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/19/17.
 */
public interface MatchmakingApplicationConfigurationClient extends RestService {

    @POST
    @Path("application/{applicationNameOrId}/configuration/matchmaking")
    void createApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            MatchmakingApplicationConfiguration matchmakingApplicationConfiguration,
            MethodCallback<MatchmakingApplicationConfiguration> matchmakingApplicationConfigurationMethodCallback);

    @GET
    @Path("application/{applicationNameOrId}/configuration/matchmaking/{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<MatchmakingApplicationConfiguration> matchmakingApplicationConfigurationMethodCallback);

    @PUT
    @Path("application/{applicationNameOrId}/configuration/matchmaking/{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MatchmakingApplicationConfiguration matchmakingApplicationConfiguration,
            MethodCallback<MatchmakingApplicationConfiguration> matchmakingApplicationConfigurationMethodCallback);

    @DELETE
    @Path("application/{applicationNameOrId}/configuration/matchmaking/{applicationProfileNameOrId}")
    void deleteApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
