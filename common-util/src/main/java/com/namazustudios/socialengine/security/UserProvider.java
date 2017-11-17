package com.namazustudios.socialengine.security;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

/**
 * Iterates all bound {@link UserAuthenticationMethod}s currently available and returns the
 * {@link User} instance determined by a combindation of headers or other authentication methods.
 *
 * If no suitable authentication method is found, then this will resort to
 * {@link UserAuthenticationMethod#UNPRIVILEGED}.
 *
 */
public class UserProvider implements Provider<User> {

    private static final Logger logger = LoggerFactory.getLogger(UserProvider.class);

    private Set<UserAuthenticationMethod> supportedAuthenticationMethods;

    @Override
    public User get() {

        for (final UserAuthenticationMethod method : getSupportedAuthenticationMethods()) {
            try {
                return method.attempt();
            } catch (ForbiddenException ex) {
                logger.debug("Failed authentication method {}", method, ex);
                continue;
            }
        }

        return UserAuthenticationMethod.UNPRIVILEGED.attempt();

    }

    public Set<UserAuthenticationMethod> getSupportedAuthenticationMethods() {
        return supportedAuthenticationMethods;
    }

    @Inject
    public void setSupportedAuthenticationMethods(Set<UserAuthenticationMethod> supportedAuthenticationMethods) {
        this.supportedAuthenticationMethods = supportedAuthenticationMethods;
    }

}
