package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Created by patricktwohig on 5/4/15.
 */
@Path("user")
public interface UserClient extends RestService {

    /**
     * Makes a round-trip call to refresh the currently logged-in user.
     *
     * @param userMethodCallback
     */
    @GET
    @Path("me")
    void refreshCurrentUser(MethodCallback<User> userMethodCallback);

    /**
     * Creates a new user given the User object, password, and the method callback.
     * @param user the User object
     * @param password the new user's password
     * @param userMethodCallback the callback
     */
    @POST
    void craeteNewUser(
            User user,
            @QueryParam("password") String password,
            MethodCallback<User> userMethodCallback);

}
