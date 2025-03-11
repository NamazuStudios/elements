package dev.getelements.elements.sdk.model.security;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.profile.Profile;

import java.util.Optional;

/**
 * Used to identify the {@link Profile} of the {@link User} making the current request.
 */
@FunctionalInterface
public interface ProfileIdentificationMethod {

    /**
     * Attempts to identify the profile.  If this process fails, then this must return an object equivalent to
     * {@link Optional#empty()}. Additional methods may be attempted until all possible methods are exhausted or a
     * suitable {@link Profile} is found.
     *
     * @return the {@link Profile}, never null
     */
    Optional<Profile> attempt();

    /**
     * The default {@link ProfileIdentificationMethod}, which simply throws an instance of {@link NotFoundException}.
     */
    ProfileIdentificationMethod UNIDENTIFIED = Optional::empty;

}
