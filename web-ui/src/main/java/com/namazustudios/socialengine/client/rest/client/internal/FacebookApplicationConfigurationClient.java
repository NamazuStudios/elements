package com.namazustudios.socialengine.client.rest.client.internal;

import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/19/17.
 */
public interface FacebookApplicationConfigurationClient extends RestService {

    @POST
    @Path("application/{applicationNameOrId}/configuration/facebook")
    void createApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            FacebookApplicationConfiguration facebookApplicationConfiguration,
            MethodCallback<FacebookApplicationConfiguration> facebookApplicationConfigurationMethodCallback);

    @GET
    @Path("application/{applicationNameOrId}/configuration/facebook/{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<FacebookApplicationConfiguration> facebookApplicationConfigurationMethodCallback);

    @PUT
    @Path("application/{applicationNameOrId}/configuration/facebook/{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            FacebookApplicationConfiguration facebookApplicationConfiguration,
            MethodCallback<FacebookApplicationConfiguration> facebookApplicationConfigurationMethodCallback);

    @DELETE
    @Path("application/{applicationNameOrId}/configuration/facebook/{applicationProfileNameOrId}")
    void deleteApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
