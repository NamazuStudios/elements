package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Created by patricktwohig on 4/16/15.
 */
@Path("session")
@Options()
public interface LoginClient extends RestService {

    /**
     * Invokes GET /session
     *
     * @param userId query param for username
     * @param password query param for apssword
     * @param methodCallback the method callback
     */
    @GET
    void login(@QueryParam("userId") final String userId,
               @QueryParam("password") final String password,
               final MethodCallback<User> methodCallback);

    /**
     * Destroys the user's current session.
     *
     * @param methodCallback
     */
    @DELETE
    void logout(final MethodCallback<Void> methodCallback);

}
