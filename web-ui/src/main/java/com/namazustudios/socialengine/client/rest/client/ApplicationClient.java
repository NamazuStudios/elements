package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import org.fusesource.restygwt.client.MethodCallback;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * Created by patricktwohig on 6/1/17.
 */
@Path("application")
public interface ApplicationClient {

    /**
     * Gets a listing of applications, filtering by query.
     *
     * @param offset
     * @param count
     * @param query
     * @param paginationMethodCall
     */
    @GET
    void getApplications(
            final int offset,
            final int count, String query,
            final MethodCallback<Pagination<Application>> paginationMethodCall);

    /**
     * Gets a listing of applications.
     *
     * @param offset the offset
     * @param count the count (max) to fetch
     * @param paginationMethodCall the callback
     */
    @GET
    void getApplications(
            final int offset,
            final int count,
            final MethodCallback<Pagination<Application>> paginationMethodCall);

    /**
     * Gets a specific application by name or ID.
     *
     * @param applicationId
     * @param applicationMethodCallback
     */
    @GET
    void getApplication(
            final String applicationId,
            final MethodCallback<Application> applicationMethodCallback);

    /**
     * Creates a new application.
     *
     * @param application
     */
    @POST
    void createApplication(Application application);

    /**
     * Updates an existing application, given the ID and application model object.
     *
     * @param nameOrId the name or ID of the application
     * @param application the application itself
     */
    @PUT
    void updateApplication(final String nameOrId, final Application application);

    /**
     * Deletes an application with the supplied name or ID.
     *
     * @param nameOrId the name or ID of the application
     */
    @PUT
    void deleteApplication(final String nameOrId);

}
