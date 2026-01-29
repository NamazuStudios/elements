package dev.getelements.elements.rest.followers;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.profile.Profile;

import dev.getelements.elements.sdk.service.follower.FollowerService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("followee")
public class FolloweesResource {

    private FollowerService followerService;

    @GET
    @Path("{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Search Followees",
            description = "Searches for all profiles that are being followed by the given profile id.")
    public Pagination<Profile> getFollowees(
            @PathParam("profileId") final String profileId,
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return getFollowerService().getFollowees(profileId, offset, count);

    }

    public FollowerService getFollowerService() {
        return followerService;
    }

    @Inject
    public void setFollowerService(FollowerService followerService) {
        this.followerService = followerService;
    }

}
