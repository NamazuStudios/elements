package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.gameon.game.AppBuildType;
import com.namazustudios.socialengine.model.gameon.game.DeviceOSType;
import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.gameon.session")
})
public interface GameOnSessionService {

    /**
     * Gets a {@link Pagination<GameOnSession>} instance.
     *
     * @param offset the offset in the data set
     * @param count the number of results to return per page
     *
     * @return the {@link Pagination<GameOnSession>}, never null
     */
    Pagination<GameOnSession> getGameOnSessions(int offset, int count);

    /**
     * Gets a {@link Pagination<GameOnSession>} instance, filtered by the supplied search query.
     *
     * @param offset the offset in the data set
     * @param count the number of results to return per page
     *
     * @return the {@link Pagination<GameOnSession>}, never null
     */
    Pagination<GameOnSession> getGameOnSessions(int offset, int count, String search);

    /**
     * Gets the {@link GameOnSession} with the supplied id.
     *
     * @param gameOnSessionId the id of the as provided by {@link GameOnSession#getId()}
     * @return the {@link GameOnSession}, never null
     */
    GameOnSession getGameOnSession(String gameOnSessionId);

    /**
     * Gets a {@link GameOnSession} for the currently logged-in {@link Profile} filtered by the supplied
     * {@link DeviceOSType}.
     *
     * @param deviceOSType the {@link DeviceOSType}
     * @return the {@link GameOnSession}, never null
     */
    GameOnSession getCurrentGameOnSession(DeviceOSType deviceOSType);

    /**
     * Creates an intance of {@link GameOnSession} by first registring the {@link GameOnSession} with Amazon and then
     * stores the resulting instance in the database.
     *
     * @param gameOnSession the {@link GameOnSession}
     * @return the {@link GameOnSession} as recorded by the service
     */
    GameOnSession createSession(GameOnSession gameOnSession);

    /**
     * Fetches, from the database a {@link GameOnSession} and returns it.  Except for misconfiguration or network
     * errors, this call should always succeed and return an instance of {@link GameOnSession} which can be used to make
     * subsequent network calls.
     *
     * @param deviceOSType the {@link DeviceOSType} used to create or lookup the {@link GameOnSession}
     * @param appBuildType
     * @return the {@link GameOnSession}, never null
     */
    GameOnSession createOrGetCurrentSession(DeviceOSType deviceOSType, AppBuildType appBuildType);

    /**
     * Refreshes the supplied {@link GameOnSession} and returns the renewed version of it.
     *
     * @param gameOnSession the {@link GameOnSession} to refresh
     * @return the {@link GameOnSession}
     */
    GameOnSession refreshExpiredSession(GameOnSession gameOnSession);

    /**
     * Deletes the {@link GameOnSession} with the supplied id as determined by {@link GameOnSession#getId()}
     *
     * @param gameOnSessionId
     */
    void deleteSession(String gameOnSessionId);

}
