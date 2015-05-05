package com.namazustudios.socialengine.client.rest;

import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

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

}
