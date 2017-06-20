package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/19/17.
 */
@Path("application/{applicationNameOrId}/configuration")
public interface FacebookApplicationConfigurationClient extends RestService {

    @POST
    @Path("facebook")
    void createApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            FacebookApplicationConfiguration facebookApplicationConfiguration,
            MethodCallback<FacebookApplicationConfiguration> facebookApplicationConfigurationMethodCallback);

    @GET
    @Path("facebook/{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<FacebookApplicationConfiguration> facebookApplicationConfigurationMethodCallback);

    @PUT
    @Path("facebook/{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            FacebookApplicationConfiguration facebookApplicationConfiguration,
            MethodCallback<FacebookApplicationConfiguration> facebookApplicationConfigurationMethodCallback);

    @DELETE
    @Path("facebook")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
