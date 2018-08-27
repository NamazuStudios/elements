package com.namazustudios.socialengine.client.rest.client.internal;

import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/19/17.
 */
public interface IosApplicationConfigurationClient extends RestService {

    @POST
    @Path("application/{applicationNameOrId}/configuration/ios")
    void createApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            IosApplicationConfiguration iosApplicationConfiguration,
            MethodCallback<IosApplicationConfiguration> iosApplicationConfigurationMethodCallback);

    @GET
    @Path("application/{applicationNameOrId}/configuration/ios/{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<IosApplicationConfiguration> iosApplicationConfigurationMethodCallback);

    @PUT
    @Path("application/{applicationNameOrId}/configuration/ios/{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            IosApplicationConfiguration iosApplicationConfiguration,
            MethodCallback<IosApplicationConfiguration> iosApplicationConfigurationMethodCallback);

    @DELETE
    @Path("application/{applicationNameOrId}/configuration/ios/{applicationProfileNameOrId}")
    void deleteApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
