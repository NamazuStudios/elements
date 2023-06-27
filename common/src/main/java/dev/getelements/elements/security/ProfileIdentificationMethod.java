package dev.getelements.elements.security;

import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.profile.Profile;

/**
 * Used to identify the {@link Profile} of the {@link User} making the current request.
 */
@FunctionalInterface
public interface ProfileIdentificationMethod {

    /**
     * Attempts to identify the profile.  If this process fails, then this must throw an
     * {@link UnidentifiedProfileException} indicating so.  Additional methods may be attempted until all possible
     * methods are exhaused or a suitable {@link Profile} is found.
     *
     * @return the {@link Profile}, never null
     */
    Profile attempt() throws UnidentifiedProfileException;

    /**
     * The default {@link ProfileIdentificationMethod}, which simply throws an instance of {@link NotFoundException}.
     */
    ProfileIdentificationMethod UNIDENTIFIED = () -> { throw new UnidentifiedProfileException(); };

}
