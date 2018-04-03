package com.namazustudios.socialengine.rest.friends;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.base.Strings.repeat;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Path("friend")
@Api(value = "Friends",
     description = "Manages friend ships among users.  Friends as associated among users, each with access to the " +
                   "individual profiles therein.",
     authorizations = {@Authorization(SESSION_SECRET)})
public class FriendResource {

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Users",
            notes = "Searches all users in the system and returning the metadata for all matches against " +
                    "the given search filter.")
    public Pagination<User> getUsers(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return null;
//        return query.isEmpty() ?
//                getUserService().getUsers(offset, count) :
//                getUserService().getUsers(offset, count, search);

    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a Specific User",
            notes = "Gets a specific user by email or unique user ID.")
    public User getUser(@PathParam("name") final String name) {
        return null;
//        return getUserService().getUser(name);
    }

    @DELETE
    @Path("{fcmRegistrationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteRegistration(
            @PathParam("fcmRegistrationId")
            final String fcmRegistrationId,
            final FCMRegistration fcmRegistration) {
//        getFcmRegistrationService().deleteRegistration(fcmRegistrationId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
