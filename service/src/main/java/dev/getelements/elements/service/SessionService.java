package dev.getelements.elements.service;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

@Expose({
    @ModuleDefinition(value = "namazu.elements.service.session"),
    @ModuleDefinition(
        value = "namazu.elements.service.unscoped.session",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
public interface SessionService {

    /**
     * Finds an instance of {@link Session} based on the id, as determined by
     * {@link SessionCreation#getSessionSecret()}.  In addition to performing a check for a valid {@link Session}, this
     * will reset the expiry of the {@link Session}.
     *
     * @param sessionSecret the {@link Session} identifier
     *
     * @return the {@link Session}, never null.  Throws the appropriate exception if session isn't found.
     *
     */
    Session checkAndRefreshSessionIfNecessary(String sessionSecret);

    /**
     * Destroys all sessions for the given ID as returned by {@link User#getId()}
     * @param userId the user Id
     */
    void destroySessions(final String userId);

    /**
     * Destroys the {@link Session} instance currently in-use for the specific user id as returned by
     * {@link User#getId()}
     *
     * @param userId the user Id
     * @param sessionSecret the session secret
     */
    void destroySession(final String userId, String sessionSecret);

}
