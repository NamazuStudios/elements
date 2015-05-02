package com.namazustudios.promotion.rest;

import com.google.common.base.Strings;
import com.namazustudios.promotion.exception.InvalidDataException;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 3/25/15.
 */
@Path("user")
public class UserResource {

    @Inject
    private UserService userService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Pagination<User> getUsers(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("count") @DefaultValue("20") int count) {

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
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("name") final String name) {
        return userService.getUser(name);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public User createUser(final User user) {
        return userService.createUser(user);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUser(final User user) {
        return userService.updateUser(user);
    }

    @PUT
    @Path("{name}/password")
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUserPassword(@PathParam("name") String name,
                                   @QueryParam("password") String password) {

        name = Strings.nullToEmpty(name).trim();
        password = Strings.nullToEmpty(password).trim();

        if (Strings.isNullOrEmpty(name)) {
            throw new InvalidDataException("Invalid user name.");
        }

        if (Strings.isNullOrEmpty(password)) {
            throw new InvalidDataException("Password must be specified.", password);
        }

        return userService.updateUserPassword(name, password);

    }

    @DELETE
    @Path("{name}")
    public void deactivateUser(@PathParam("name") final String name) {
        userService.deleteUser(name);
    }

}
