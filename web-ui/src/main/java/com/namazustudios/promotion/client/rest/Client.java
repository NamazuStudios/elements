package com.namazustudios.promotion.client.rest;

import com.namazustudios.promotion.model.User;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Created by patricktwohig on 4/16/15.
 */
@Singleton
public interface Client extends RestService {

    /**
     * Invokes GET /session
     *
     * @param userId query param for username
     * @param password query param for apssword
     * @param methodCallback the method callback
     */
    @GET
    @Path("session")
    void login(@QueryParam("userId") final String userId,
               @QueryParam("password") final String password,
               final MethodCallback<User> methodCallback);

}
