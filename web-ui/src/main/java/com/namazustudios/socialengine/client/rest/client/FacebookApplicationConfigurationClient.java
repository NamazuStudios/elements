package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/19/17.
 */
@Path("application/{applicationNameOrId}/configuration/facebook")
public interface FacebookApplicationConfigurationClient extends RestService {

    @POST
    void updateApplicationProfile(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            FacebookApplicationConfiguration facebookApplicationConfiguration,
            MethodCallback<FacebookApplicationConfiguration> facebookApplicationConfigurationMethodCallback);

    @GET
    @Path("{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<FacebookApplicationConfiguration> facebookApplicationConfigurationMethodCallback);

    @PUT
    @Path("{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            FacebookApplicationConfiguration facebookApplicationConfiguration,
            MethodCallback<FacebookApplicationConfiguration> facebookApplicationConfigurationMethodCallback);

    @DELETE
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
