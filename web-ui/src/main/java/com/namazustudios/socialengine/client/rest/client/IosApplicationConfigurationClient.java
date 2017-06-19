package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/19/17.
 */
@Path("application/{applicationNameOrId}/configuration/ios")
public interface IosApplicationConfigurationClient extends RestService {

    @POST
    void updateApplicationProfile(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            IosApplicationConfiguration iosApplicationConfiguration,
            MethodCallback<IosApplicationConfiguration> iosApplicationConfigurationMethodCallback);

    @GET
    @Path("{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<IosApplicationConfiguration> iosApplicationConfigurationMethodCallback);

    @PUT
    @Path("{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            IosApplicationConfiguration iosApplicationConfiguration,
            MethodCallback<IosApplicationConfiguration> iosApplicationConfigurationMethodCallback);

    @DELETE
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
