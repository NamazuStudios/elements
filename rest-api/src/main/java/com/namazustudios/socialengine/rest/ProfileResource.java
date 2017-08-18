package com.namazustudios.socialengine.rest;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.service.ProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by patricktwohig on 6/27/17.
 */
@Api(value = "Profiles",
     description = "Allows for the manipulation of Profile objects.  Profile objects store the " +
                   "basic information for the users in the system as they are associated with " +
                   "Applications.",
     authorizations = {@Authorization(EnhancedApiListingResource.FACBOOK_OAUTH_KEY)})
@Path("profile")
public class ProfileResource {

    private ProfileService profileService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Profiles",
            notes = "Searches all users in the system and returning the metadata for all matches against " +
                    "the given search filter.")
    public Pagination<Profile> getProfiles(
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

        return query.isEmpty() ?
                getProfileService().getProfiles(offset, count) :
                getProfileService().getProfiles(offset, count, search);

    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a Specific Profile",
            notes = "Gets a specific profile by profile ID.")
    public Profile getProfile(@PathParam("name") String profileId) {

        profileId = Strings.nullToEmpty(profileId).trim();

        if (profileId.isEmpty()) {
            throw new NotFoundException();
        }

        return getProfileService().getProfile(profileId);

    }

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the current Profile",
            notes = "This is a special endpoing which fetches the current Profile based " +
                    "on current auth credentials.  This considers the currently loggged-in Dser " +
                    "as well as the Application or Application Configuration against which the " +
                    "User is operating.  This may not be availble, in which case the appopraite " +
                    "error is rasied.")
    public Profile getCurrentProfile() {
        return getProfileService().getCurrentProfile();
    }

    @PUT
    @Path("{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Profile",
            notes = "Suppplying a profile Object will attempt to update the profile.  The call " +
                    "will return the profile as it was written to the database.")
    public Profile updateProfile(final Profile profile, @PathParam("profileId") String profileId) {

        getValidationHelper().validateModel(profile);
        profileId = Strings.nullToEmpty(profileId).trim();

        if (Strings.isNullOrEmpty(profileId)) {
            throw new NotFoundException("Profile not found.");
        } else if (!(Objects.equals(profile.getId(), profileId))) {
            throw new InvalidDataException("Profile id does not match path.");
        }

        return getProfileService().updateProfile(profile);

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a User",
            notes = "Supplying the user object, this will update the user with the new information supplied " +
                    "in the body of the request.  Optionally, the user's password may be provided.")
    public Profile createProfile(final Profile profile) {

        getValidationHelper().validateModel(profile);

        final String profileId = nullToEmpty(profile.getId()).trim();

        if (!profileId.isEmpty()) {
            throw new BadRequestException("Profile ID must be blank.");
        }

        return getProfileService().createProfile(profile);

    }

    @DELETE
    @Path("{profileId}")
    @ApiOperation(value = "Deletes a Profile",
            notes = "Deletes and permanently removes the Profile from the server.  The server may" +
                    "keep some record around to preserve relationships and references, but " +
                    "this profile will not be accessible again until it is recreated.")
    public void deactivateProfile(@PathParam("profileId") final String profileId) {
        getProfileService().deleteProfile(profileId);
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    @Inject
    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
