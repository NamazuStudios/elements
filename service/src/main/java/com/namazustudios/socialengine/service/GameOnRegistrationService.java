package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.gameon.game.GameOnRegistration;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Manages instances of {@link GameOnRegistration}.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.gameon.registration"),
    @ExposedModuleDefinition(
        value = "namazu.elements.service.unscoped.gameon.registration",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
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
     * Creates, or gets the current, {@link GameOnRegistration} instance.  With the exception of misconfiguration or
     * network error, this should reutrn an instance of {@link GameOnRegistration}.
     *
     * @return the {@link GameOnRegistration} instance
     */
    GameOnRegistration createOrGetCurrentRegistration();

    /**
     * Deletes the {@link GameOnRegistration} with the supplied id.
     *
     * @param gameOnRegistrationId
     */
    void deleteRegistration(String gameOnRegistrationId);

}
