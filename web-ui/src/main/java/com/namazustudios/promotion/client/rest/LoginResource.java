package com.namazustudios.promotion.client.rest;

import com.gwtplatform.dispatch.rest.shared.RestAction;
import com.namazustudios.promotion.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * Created by patricktwohig on 5/1/15.
 */
@Produces("application/json")
public interface LoginResource {

    @GET
    @Path("session")
    RestAction<User> login(
            @QueryParam("userId") final String userId,
            @QueryParam("password") final String password);

}

