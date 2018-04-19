package com.namazustudios.socialengine.rest.friends;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import com.namazustudios.socialengine.service.FacebookFriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.Headers.FACEBOOK_OAUTH_TOKEN;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Path("friend_uninvited")
@Api(value = "Friends",
     description = "Provides access to Friends who are available, but not invited to the current application." +
                      "This is useful for sending invites and cross-promotion of applications.",
     authorizations = {@Authorization(SESSION_SECRET)})
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
