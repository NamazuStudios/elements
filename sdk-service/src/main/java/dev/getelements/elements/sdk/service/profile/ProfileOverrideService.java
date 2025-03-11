package dev.getelements.elements.sdk.service.profile;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Optional;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Implements the logic to override the profile when making requests.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ProfileOverrideService {

    /**
     * Gets the profile override for the supplied user and profile ID.
     *
     * @param profileId
     * @return the profile override
     */
    Optional<Profile> findOverrideProfile(String profileId);

}
