package com.namazustudios.socialengine.client.rest.client.internal;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 6/1/17.
 */
@Path("application")
public interface ApplicationClient extends RestService {

    /**
     * Gets a listing of applications, filtering by query.
     *
     * @param offset
     * @param count
     * @param paginationMethodCall
     */
    @GET
    void getApplications(
            @QueryParam("offset") final int offset,
            @QueryParam("count")  final int count,
            MethodCallback<Pagination<Application>> paginationMethodCall);

    /**
     * Gets a listing of applications.
     *
     * @param offset the offset
     * @param count the count (max) to fetch
     * @param paginationMethodCall the callback
     */
    @GET
    void getApplications(
            @QueryParam("offset") final int offset,
            @QueryParam("count")  final int count,
            @QueryParam("search") String search,
            MethodCallback<Pagination<Application>> paginationMethodCall);

    /**
     * Gets a specific application by name or ID.
     *
     * @param nameOrId
     * @param applicationMethodCallback
     */
    @GET
    @Path("{nameOrId}")
    void getApplication(@PathParam("nameOrId") final String nameOrId,
                        MethodCallback<Application> applicationMethodCallback);

    /**
     * Creates a new application.
     *
     * @param application
     */
    @POST
    void createApplication(Application application,
                           MethodCallback<Application> applicationMethodCallback);

    /**
     * Updates an existing application, given the ID and application model object.
     *
     * @param nameOrId the name or ID of the application
     * @param application the application itself
     */
    @PUT
    @Path("{nameOrId}")
    void updateApplication(@PathParam("nameOrId") String nameOrId,
                           Application application,
                           MethodCallback<Application> applicationMethodCallback);

    /**
     * Deletes an application with the supplied name or ID.
     *
     * @param nameOrId the name or ID of the application
     */
    @DELETE
    @Path("{nameOrId}")
    void deleteApplication(@PathParam("nameOrId") String nameOrId,
                           MethodCallback<Void> applicationMethodCallback);

}
