package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.ProfileOverrideService;

import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Implementation for users with {@link User.Level#UNPRIVILEGED} users.
 */
public class AnonProfileOverrideService implements ProfileOverrideService {

    /**
     * Always returns null because non-auth'd users have no profile.
     *
     * @param profileId the profile ID
     * @return null always
     */
    @Override
    public Optional<Profile> findOverrideProfile(final String profileId) {
        return empty();
    }

}
