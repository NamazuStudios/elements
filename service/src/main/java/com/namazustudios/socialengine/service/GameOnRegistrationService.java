package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;

/**
 * Manages instances of {@link GameOnRegistration}.
 */
public interface GameOnRegistrationService {

    /**
     * Gets a {@link GameOnRegistration} instance with a specific id.
     *
     * @param gameOnRegistrationId the {@link GameOnRegistration#getId()}
     * @return the {@link GameOnRegistration}, never null
     * @throws {@link NotFoundException} if the instance does not exist
     */
    GameOnRegistration getGameOnRegistration(String gameOnRegistrationId);

    /**
     * Gets the current {@link GameOnRegistration}, if available.
     *
     * @return the current {@link GameOnRegistration}
     */
    GameOnRegistration getCurrentGameOnRegistration();

    /**
     * Lists all {@link GameOnRegistration} instances.
     *
     * @param offset the offset in the dataset
     * @param count the count in the dataset
     * @return the a {@link Pagination<GameOnRegistration>}
     */
    Pagination<GameOnRegistration> getGameOnRegistrations(int offset, int count);

    /**
     * Lists all {@link GameOnRegistration} instances.  Optionally specifying a search query.
     *
     * @param offset the offset in the dataset
     * @param count the count in the dataset
     * @return the a {@link Pagination<GameOnRegistration>}
     */
    Pagination<GameOnRegistration> getGameOnRegistrations(int offset, int count, String search);

    /**
     * Creates a {@link GameOnRegistration} by first making API calls to Amazon GameOn and then subsequently storing
     * the result in the database.  Any client may access the {@link GameOnRegistration} for subsequent use.
     *
     * @param gameOnRegistration the {@link GameOnRegistration} instance
     * @return the {@link GameOnRegistration} as it was written to the database
     */
    GameOnRegistration createRegistration(GameOnRegistration gameOnRegistration);

    /**
     * Deletes the {@link GameOnRegistration} with the supplied id.
     *
     * @param gameOnRegistrationId
     */
    void deleteRegistration(String gameOnRegistrationId);

}
