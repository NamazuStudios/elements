package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * Basic code to fetch {@link ApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 6/1/17.
 */
@Path("application")
public interface ApplicationConfigurationClient extends RestService {

    /**
     * Gets all application profiles.
     *
     * @param offset
     * @param count
     * @param methodCallback
     */
    @GET
    @Path("{applicationNameOrId}/profile")
    void getAllProfiles(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @QueryParam("offset") int offset,
            @QueryParam("count")  int count,
            MethodCallback<Pagination<ApplicationConfiguration>> methodCallback);

    /**
     * Gets all application profiles, filtered by search query.
     *
     * @param offset
     * @param count
     * @param search
     * @param methodCallback
     */
    @GET
    @Path("{applicationNameOrId}/profile")
    void getAllProfiles(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @QueryParam("offset") int offset,
            @QueryParam("count")  int count,
            @QueryParam("search") String search,
            MethodCallback<Pagination<ApplicationConfiguration>> methodCallback);

}
