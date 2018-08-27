package com.namazustudios.socialengine.client.rest.client.internal;

import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

public interface FirebaseApplicationConfigurationClient extends RestService {

    @POST
    @Path("application/{applicationNameOrId}/configuration/firebase")
    void createApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            FirebaseApplicationConfiguration firebaseApplicationConfiguration,
            MethodCallback<FirebaseApplicationConfiguration> firebaseApplicationConfigurationMethodCallback);

    @GET
    @Path("application/{applicationNameOrId}/configuration/firebase/{applicationProfileNameOrId}")
    void getApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<FirebaseApplicationConfiguration> firebaseApplicationConfigurationMethodCallback);

    @PUT
    @Path("application/{applicationNameOrId}/configuration/firebase/{applicationProfileNameOrId}")
    void updateApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            FirebaseApplicationConfiguration firebaseApplicationConfiguration,
            MethodCallback<FirebaseApplicationConfiguration> firebaseApplicationConfigurationMethodCallback);

    @DELETE
    @Path("application/{applicationNameOrId}/configuration/firebase/{applicationProfileNameOrId}")
    void deleteApplicationConfiguration(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") String applicationProfileNameOrId,
            MethodCallback<Void> voidMethodCallback);

}
