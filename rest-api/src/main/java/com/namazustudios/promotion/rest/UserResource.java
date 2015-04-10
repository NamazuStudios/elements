package com.namazustudios.promotion.rest;

import com.namazustudios.promotion.exception.InvalidParameterException;
import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.UserService;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by patricktwohig on 3/25/15.
 */
@Path("user")
public class UserResource {

    @Inject
    private UserService userService;

    @GET
    public Pagination<User> getUsers(
            @PathParam("offset") @DefaultValue("0") int offset,
            @PathParam("count") @DefaultValue("20") int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return userService.getUsers(offset, count);

    }

    @GET
    @Path("{name}")
    public User getUser(@PathParam("name") final String name) {
        return userService.getUser(name);
    }

    @POST
    public User createUser(final User user) {
        return userService.createUser(user);
    }

    @PUT
    public User updateUser(final User user) {
        return userService.updateUser(user);
    }

    @PUT
    @Path("{name}/password")
    public User updateUserPassword(@PathParam("name") final String name, final String password) {
        return userService.updateUserPassword(name, password);
    }

    @DELETE
    @Path("{name}")
    public void deactivateUser(@PathParam("name") final String name) {
        userService.deleteUser(name);
    }

}
