package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.application.PSNApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/19/17.
 */
@Path("application/{applicationNameOrId}/configuration/psn")
public interface PSNApplicationConfigurationClient extends RestService {

    @POST
    void updateApplicationProfile(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            PSNApplicationConfiguration psnApplicationConfiguration,
            MethodCallback<PSNApplicationConfiguration> psnApplicationConfigurationMethodCallback);

    @GET
    @Path("{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<PSNApplicationConfiguration> psnApplicationConfigurationMethodCallback);

    @PUT
    @Path("{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            PSNApplicationConfiguration psnApplicationConfiguration,
            MethodCallback<PSNApplicationConfiguration> psnApplicationConfigurationMethodCallback);

    @DELETE
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
