package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.profile.Profile;

import java.util.Optional;

/**
 * Implements the logic to override the profile when making requests.
 */
public interface ProfileOverrideService {

    /**
     * Gets the profile override for the supplied user and profile ID.
     *
     * @param profileId
     * @return the profile override
     */
    Optional<Profile> findOverrideProfile(String profileId);

}
