package dev.getelements.elements.service;

import java.io.IOException;
import java.util.Optional;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.profile.CreateProfileRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.profile.UpdateProfileImageRequest;
import dev.getelements.elements.model.profile.UpdateProfileRequest;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Manages instances of {@link Profile}.
 *
 * Created by patricktwohig on 6/27/17.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.profile"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.profile",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.profile",
                deprecated = @DeprecationDefinition("Use eci.elements.service.profile instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.profile",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.unscoped.profile instead.")
        )
})
public interface ProfileService {

    /**
     * Lists all {@link Profile} instances starting with the offset and count.
     *
     * @param offset the offset
     * @param count the count
     * @param applicationNameOrId the application name or ID to use when fetching the profiles
     * @param userId the userId which to use when filtering.  If null, then no filtering will be applied.
     * @param lowerBoundTimestamp
     * @param upperBoundTimestamp
     * @return a {@link Pagination} of {@link Profile} instances
     */
    Pagination<Profile> getProfiles(int offset, int count,
                                    String applicationNameOrId, String userId,
                                    Long lowerBoundTimestamp, Long upperBoundTimestamp);

    /**
     * Lists all {@link Profile} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param search
     * @return
     */
    Pagination<Profile> getProfiles(int offset, int count, String search);

    /**
     * Redacts any private information from the {@link Profile} and returns either a new instance or the current
     * instance modified.
     *
     * The default implementation simply sets the user to null and returns the supplied instance.  Other implementations
     * such as those used for superuser access may redact information differently.
     *
     * @param profile the {@link Profile} from which to redact private information
     * @return a {@link Profile} with the information redacted (may be the same instance provided
     */
    default Profile redactPrivateInformation(final Profile profile) {
        profile.setUser(null);
        return profile;
    }

    /**
     * Fetches a specific {@link Profile} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param profileId the profile ID
     * @return the {@link Profile}, never null
     */
    Profile getProfile(String profileId);

    /**
     * Returns the {@link Profile} of the currently logged-in user.  If the user has no
     * profile, or that information is not available, then this will throw an exception.
     *
     * Using a profile is not required for all calls.  Therefore, it should be expected
     * that this will throw {@link NotFoundException} under normal circumstances.
     *
     * @return the {@link Profile}, or null, if no profile is found.
     */
    Profile getCurrentProfile();

    /**
     * Finds the currently active profile, if any.
     * @return an {@link Optional<Profile>}
     */
    Optional<Profile> findCurrentProfile();

    /**
     * Updates the supplied {@link Profile}.  The {@link Profile#getId()} method is
     * used to key the {@link Profile}.
     *
     * @param profileId the profile id of the {@link Profile} to update
     * @param profileRequest the {@link UpdateProfileRequest} with the information to update
     * @return the {@link Profile} as it was changed by the service.
     */
    Profile updateProfile(String profileId, UpdateProfileRequest profileRequest);

    /**
     * Creates a new profile.  The ID of the profile, as specified by {@link Profile#getId()},
     * should be null and will be assigned.
     *
     * @param profileRequest the {@link CreateProfileRequest} with the information to create
     * @return the {@link Profile} as it was created by the service.
     */
    Profile createProfile(CreateProfileRequest profileRequest);

    /**
     * Deletes the {@link Profile} with the supplied profile ID.
     *
     * @param profileId the profile ID.
     */
    void deleteProfile(String profileId);

    /**
     * Update profile image related fields
     *
     * @param updateProfileImageRequest request with image object values
     */
    Profile updateProfileImage(String profileId, UpdateProfileImageRequest updateProfileImageRequest) throws IOException;

}
