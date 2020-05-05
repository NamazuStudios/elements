package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.gameon.game.GameOnRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.Expose;

@Expose(modules = "namazu.elements.dao.gameon.registration")
public interface GameOnRegistrationDao {

    /**
     * Gets the single specific {@link GameOnRegistration} instance for the supplied {@link Profile}.
     *
     * @param profile the {@link Profile} associated with the supplied {@link GameOnRegistration}
     * @return the {@link GameOnRegistration} instance
     */
    GameOnRegistration getRegistrationForProfile(Profile profile);

    /**
     * Gets a specific {@link GameOnRegistration}, ensuring that the supplied {@link Profile} may access the
     * stored instance of {@link GameOnRegistration}.
     *
     * @param user the {@link Profile}
     * @param gameOnRegistrationId the gameon registration ID
     * @return the {@link GameOnRegistration} instance
     */
    GameOnRegistration getRegistrationForUser(User user, String gameOnRegistrationId);

    /**
     * Gets all {@link GameOnRegistration} instances, filtered by the supplied {@link User}.
     *
     * @param user the {@link User}
     * @param offset the offset
     * @param count the count
     * @return return a {@link Pagination<GameOnRegistration>}
     */
    Pagination<GameOnRegistration> getRegistrationsForUser(User user, int offset, int count);

    /**
     * Gets all {@link GameOnRegistration} instances, filtered by the supplied {@link User} and search criteria.
     *
     * @param user the {@link User}
     * @param offset the offset
     * @param count the count
     * @return return a {@link Pagination<GameOnRegistration>}
     */
    Pagination<GameOnRegistration> getRegistrationsForUser(User user, int offset, int count, String search);

    /**
     * Creates a new {@link GameOnRegistration} and stores it in the database.
     *
     * @param gameOnRegistration the {@link GameOnRegistration}
     * @return the {@link GameOnRegistration} instance as it was written to the database
     */
    GameOnRegistration createRegistration(GameOnRegistration gameOnRegistration);

    /**
     * Gets the {@link GameOnRegistration} for the supplied external player id as returned by
     * {@link GameOnRegistration#getExternalPlayerId()}.
     *
     * @param externalPlayerId the external player ID
     * @return the {@link GameOnRegistration}, never null
     *
     */
    GameOnRegistration getRegistrationForExternalPlayerId(String externalPlayerId);

    /**
     * Deletes the {@link GameOnRegistration} with the supplied id, checking to ensure that the {@link Profile} owns
     * the {@link GameOnRegistration} associated with the id.
     *  @param user the {@link Profile}
     * @param gameOnRegistrationId the {@link GameOnRegistration} id.
     */
    void deleteRegistrationForUser(User user, String gameOnRegistrationId);

}
