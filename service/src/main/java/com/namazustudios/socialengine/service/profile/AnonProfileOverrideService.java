package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.ProfileOverrideService;

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
    public Profile findOverrideProfile(final String profileId) {
        return null;
    }

}
