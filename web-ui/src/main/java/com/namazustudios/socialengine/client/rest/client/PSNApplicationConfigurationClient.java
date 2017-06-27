package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.application.PSNApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/19/17.
 */
public interface PSNApplicationConfigurationClient extends RestService {

    @POST
    @Path("application/{applicationNameOrId}/configuration/psn")
    void createApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            PSNApplicationConfiguration psnApplicationConfiguration,
            MethodCallback<PSNApplicationConfiguration> psnApplicationConfigurationMethodCallback);

    @GET
    @Path("application/{applicationNameOrId}/configuration/psn/{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<PSNApplicationConfiguration> psnApplicationConfigurationMethodCallback);

    @PUT
    @Path("application/{applicationNameOrId}/configuration/psn/{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            PSNApplicationConfiguration psnApplicationConfiguration,
            MethodCallback<PSNApplicationConfiguration> psnApplicationConfigurationMethodCallback);

    @DELETE
    @Path("application/{applicationNameOrId}/configuration/psn/{applicationProfileNameOrId}")
    void deleteApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
