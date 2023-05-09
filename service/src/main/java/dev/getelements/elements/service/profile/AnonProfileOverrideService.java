package dev.getelements.elements.service.profile;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.service.ProfileOverrideService;

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
