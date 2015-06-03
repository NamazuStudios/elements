package com.namazustudios.socialengine.client.rest.client;

import com.google.gwt.dev.generator.ast.MethodCall;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by patricktwohig on 5/4/15.
 */
@Path("user")
public interface UserClient extends RestService {

    /**
     * Gets the users in the system in a paginated fashion.
     *
     * @param offset the offset from zero
     * @param count the number of results to return
     */
    @GET
    void getUsers(@QueryParam("offset")int offset,
                  @QueryParam("count") int count,
                  MethodCallback<Pagination<User>> users);

    /**
     * Gets the users in the system in a paginated fashion.  Optionally, this
     * allows for the specification of an additional "Search" parameter.
     *
     * @param offset the offset from zero
     * @param count the number of results to return
     * @param search a search query for the users
     *
     */
    @GET
    void getUsers(@QueryParam("offset") int offset,
                  @QueryParam("count")  int count,
                  @QueryParam("search") String search,
                  MethodCallback<Pagination<User>> users);

    /**
     * Makes a round-trip call to refresh the currently logged-in user.
     *
     * @param userMethodCallback the user method callback
     */
    @GET
    @Path("me")
    void refreshCurrentUser(MethodCallback<User> userMethodCallback);

    /**
     * Uses a GET request to fetch the user with the given user name.
     *
     * @param userName the user name
     * @param userMethodCallback the user method callback
     */
    @GET
    @Path("{userName}")
    void getUser(final @PathParam("userName") String userName, MethodCallback<User> userMethodCallback);

    /**
     * Creates a new user given the User object, password, and the method callback.
     * @param user the User object
     * @param password the new user's password
     * @param userMethodCallback the callback
     */
    @POST
    void createNewUser(
            User user,
            @QueryParam("password") String password,
            MethodCallback<User> userMethodCallback);

    /**
     * Updates a user with the given credentials.
     *
     * @param name the user name or email address
     * @param password the user's password
     * @param user the user JSON object
     * @param userMethodCallback the method callback
     */
    @PUT
    @Path("{name}")
    void updateUser(
            @PathParam("name") String name,
            @QueryParam("password") String password,
            final User user,
            final MethodCallback<User> userMethodCallback);

}
