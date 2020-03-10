package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Implements the logic to override the profile when making requests.
 */
public interface ProfileOverrideService {

    /**
     * Gets the profile override for the supplied user and profile ID.
     *
     * @param profileId
     * @return the profile override, or null if no override exists.
     */
    Profile findOverrideProfile(String profileId);

}
