package dev.getelements.elements.rest.friends;

import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.friend.FacebookFriend;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.FacebookFriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.Headers.FACEBOOK_OAUTH_TOKEN;

@Path("friend_uninvited")
@Api(value = "Friends",
     description = "Provides access to Friends who are available, but not invited to the current application." +
                      "This is useful for sending invites and cross-promotion of applications.",
     authorizations = {@Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)})
public class UninvitedFriendResource {

    private FacebookFriendService facebookFriendService;

    @GET @Path("facebook")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Get Univited Facebook Friends",
        notes = "Returns the list of all Facebook friends who have not created a profile for the current application.")
    public Pagination<FacebookFriend> getUnivitedFacebookFriends(
                @HeaderParam(FACEBOOK_OAUTH_TOKEN)              final String facebookOAuthAccessToken,
                @QueryParam("application")                      final String applicationNameOrId,
                @QueryParam("applicationConfiguration")         final String applicationConfigurationNameOrId,
                @QueryParam("offset") @DefaultValue("0")        final int offset,
                @QueryParam("count")  @DefaultValue("20")       final int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        if (facebookOAuthAccessToken == null) {
            throw new InvalidDataException("must specify Facebook OAuth Token");
        }

        return getFacebookFriendService().getUninvitedFacebookFriends(
            applicationNameOrId,
            applicationConfigurationNameOrId,
            facebookOAuthAccessToken,
            offset, count);

    }

    public FacebookFriendService getFacebookFriendService() {
        return facebookFriendService;
    }

    @Inject
    public void setFacebookFriendService(FacebookFriendService facebookFriendService) {
        this.facebookFriendService = facebookFriendService;
    }

}
