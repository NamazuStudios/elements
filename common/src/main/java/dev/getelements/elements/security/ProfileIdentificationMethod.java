package dev.getelements.elements.security;

import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.profile.Profile;

import javax.swing.text.html.Option;
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
