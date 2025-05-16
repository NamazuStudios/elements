package dev.getelements.elements.rest.profile;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.profile.CreateProfileRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.profile.UpdateProfileImageRequest;
import dev.getelements.elements.sdk.model.profile.UpdateProfileRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.profile.ProfileService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.service.profile.UserProfileService.PROFILE_CREATED_EVENT;

/**
 * Created by patricktwohig on 6/27/17.
 */
@Path("profile")
public class ProfileResource {

    private ProfileService profileService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Search Profiles",
            description = "Searches all users in the system and returning the metadata for all matches against " +
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
    @Operation(
            summary = "Gets a Specific Profile",
            description = "Gets a specific profile by profile ID.")
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
    @Operation(
            summary = "Gets the current Profile",
            description = "This is a special endpoing which fetches the current Profile based " +
                    "on current auth credentials.  This considers the currently loggged-in Dser " +
                    "as well as the Application or Application Configuration against which the " +
                    "User is operating.  This may not be availble, in which case the appopraite " +
                    "error is rasied.")
    public Profile getCurrentProfile() {
        return getProfileService().getCurrentProfile();
    }

    @PUT
    @Path("{profileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a Profile",
            description = "Supplying an update request will attempt to update the profile.  The call " +
                    "will return the profile as it was written to the database.")
    public Profile updateProfile(
            @PathParam("profileId") String profileId,
            final UpdateProfileRequest profileRequest) {

        getValidationHelper().validateModel(profileRequest, Update.class);
        profileId = Strings.nullToEmpty(profileId).trim();

        if (Strings.isNullOrEmpty(profileId)) {
            throw new NotFoundException("Profile not found.");
        }

        return getProfileService().updateProfile(profileId, profileRequest);

    }

    @PUT
    @Path("{profileId}/image")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates a Profile image object")
    public Profile updateProfileImage(
            @PathParam("profileId") String profileId,
            final UpdateProfileImageRequest updateProfileImageRequest) throws IOException {

        getValidationHelper().validateModel(updateProfileImageRequest, Update.class);
        profileId = Strings.nullToEmpty(profileId).trim();

        if (Strings.isNullOrEmpty(profileId)) {
            throw new NotFoundException("Profile not found.");
        }

        return getProfileService().updateProfileImage(profileId, updateProfileImageRequest);

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a Profile",
            description = "Supplying the create profile request, this will update the profile with the new information supplied " +
                    "in the body of the request. This will fire an event, " + PROFILE_CREATED_EVENT + ", from the event manifest.")
    public Profile createProfile(final CreateProfileRequest profileRequest) {

        if (profileRequest.getUserId() == null && profileRequest.getUser() != null) {
            profileRequest.setUserId(profileRequest.getUser().getId());
        }

        if (profileRequest.getApplicationId() == null && profileRequest.getApplication() != null) {
            profileRequest.setApplicationId(profileRequest.getApplication().getId());
        }

        getValidationHelper().validateModel(profileRequest, Create.class);
        return getProfileService().createProfile(profileRequest);
    }

    @DELETE
    @Path("{profileId}")
    @Operation(
            summary = "Deletes a Profile",
            description = "Deletes and permanently removes the Profile from the server.  The server may" +
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
