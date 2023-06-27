package dev.getelements.elements.service;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;

/**
 * Created by patricktwohig on 4/1/15.
 */
public interface UsernamePasswordAuthService {

    /**
     * Generates an API key for the given userId and password.
     *
     * @param userId the user Id
     * @param password the user's password
     * @return the API key instance
     * @deprecated user {@link #createSessionWithLogin(String, String)}
     */
    default User loginUser(final String userId, final String password) {
        final SessionCreation sessionCreation = createSessionWithLogin(userId, password);
        return sessionCreation.getSession().getUser();
    }

    /**
     * Creates a {@link Session} with the login credentials for a {@link User}.
     *
     * @param userId the user ID
     * @param password the password
     * @return the {@link Session} created
     */
    SessionCreation createSessionWithLogin(String userId, String password);

    /**
     * Creates a {@link Session} with the login credentials for a {@link User} while also specifying the id of the
     * {@link Profile} to use.
     *
     * @param userId the user ID
     * @param password the password
     * @param profileId the profileId
     * @return the {@link Session} created
     */
    SessionCreation createSessionWithLogin(String userId, String password, String profileId);

}
