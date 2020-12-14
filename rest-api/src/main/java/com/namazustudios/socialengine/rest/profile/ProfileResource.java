package com.namazustudios.socialengine.rest.profile;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.ProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SOCIALENGINE_SESSION_SECRET;

/**
 * Created by patricktwohig on 6/27/17.
 */
@Api(value = "Profiles",
     description = "Allows for the manipulation of Profile objects.  Profile objects store the " +
                   "basic information for the users in the system as they are associated with " +
                   "Applications.",
     authorizations = {@Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("profile")
public class ProfileResource {

    private ProfileService profileService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Profiles",
            notes = "Searches all users in the system and returning the metadata for all matches against " +
                    "the given search filter. Optionally provide `before` and `after` params to specify a time range" +
                    " [`after`, `before`] for last-logged-in profiles matching in that range (inclusive). If `before`" +
                    " is not specified (or a negative number is provided) but `after` is valid, the query will return " +
                    "all records successive to the given `after` timestamp. Similarly, if `after` is not specified " +
                    "(or a negative number is provided) but `before` is valid, the query will return all records " +
                    "preceding the given `before` timestamp. Note that search and time range parameters currently " +
                    "cannot be combined in the same query.")
    public Pagination<Profile> getProfiles(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("before") final Long beforeTimestamp,
            @QueryParam("after")  final Long afterTimestamp,
            @QueryParam("application") final String applicationNameOrId,
            @QueryParam("user") final String userId,
            @QueryParam("search") final String search) {
        // Note: afterTimestamp => lower bound of time range, beforeTimestamp => upper bound of time range, i.e.:
        // [afterTimestamp, beforeTimestamp]

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        if (beforeTimestamp != null && afterTimestamp != null && afterTimestamp > beforeTimestamp) {
            throw new InvalidParameterException("Invalid range: afterTimestamp should be less than or " +
                    "equal to beforeTimestamp.");
        }

        final String query = nullToEmpty(search).trim();

        if ((beforeTimestamp != null || afterTimestamp != null) && !query.isEmpty()) {
            throw new InvalidParameterException("Time range and search parameters may not be combined.");
        }

        return query.isEmpty() ?
                getProfileService().getProfiles(
                        offset, count,
                        applicationNameOrId, userId,
                        afterTimestamp, beforeTimestamp) :
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

        getValidationHelper().validateModel(profile, Update.class);
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
                    "in the body of the request. Optionally, the user's password may be provided.")
    public Profile createProfile(final Profile profile) {

        getValidationHelper().validateModel(profile, Create.class);

        final String profileId = nullToEmpty(profile.getId()).trim();

        if (!profileId.isEmpty()) {
            throw new BadRequestException("Profile ID must be blank.");
        }

        if(profile.getEventDefinition() == null) {
            return getProfileService().createProfile(profile);
        }
        return getProfileService().createProfile(profile, profile.getEventDefinition().getModule());
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
