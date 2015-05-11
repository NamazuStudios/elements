package com.namazustudios.socialengine.client.rest.client;

import com.google.gwt.dev.generator.ast.MethodCall;
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
    @PathParam("{name}")
    void updateUser(
            @PathParam("name") String name,
            @QueryParam("password") String password,
            final User user,
            final MethodCallback<User> userMethodCallback);

}
