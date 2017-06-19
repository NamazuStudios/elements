package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/19/17.
 */
@Path("application/{applicationNameOrId}/configuration/google_play")
public interface GooglePlayApplicationConfigurationClient extends RestService {

    @POST
    void updateApplicationProfile(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            GooglePlayApplicationConfiguration googlePlayApplicationConfiguration,
            MethodCallback<GooglePlayApplicationConfiguration> googlePlayApplicationConfigurationMethodCallback);

    @GET
    @Path("{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<GooglePlayApplicationConfiguration> googlePlayApplicationConfigurationMethodCallback);

    @PUT
    @Path("{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            GooglePlayApplicationConfiguration googlePlayApplicationConfiguration,
            MethodCallback<GooglePlayApplicationConfiguration> googlePlayApplicationConfigurationMethodCallback);

    @DELETE
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
